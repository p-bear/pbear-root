package com.pbear.devtool.processor;

import com.google.auto.service.AutoService;
import com.pbear.devtool.annotation.EnablePBearServerInfoDevtool;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * 참고:
 *  - https://taes-k.github.io/2021/04/18/java-annotation-processing/
 *  - https://github.com/taes-k/sample-annotation-processing
 *  - https://www.baeldung.com/java-annotation-processing-builder
 */
@SupportedAnnotationTypes("com.pbear.devtool.annotation.EnablePBearServerInfoDevtool")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
@SuppressWarnings({"unused"})
public class ServerProperitesProcessor extends AbstractProcessor {
  private static final String SERVER_ENUM_PACKAGE_NAME = "com.pbear.devtool";
  private static final String SERVER_ENUM_CLASS_NAME = "Server";
  private static final String TAB = "  ";

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    roundEnv.getElementsAnnotatedWith(EnablePBearServerInfoDevtool.class)
        .stream()
        .filter(element -> element.getKind() == ElementKind.CLASS)
        .findFirst()
        .ifPresent(element -> this.createServerResource((TypeElement) element));

    return true;
  }

  private void createServerResource(TypeElement typeElement) {
    try {
      System.out.println(typeElement.getQualifiedName());
      File currentDir = new File(System.getProperty("user.dir"));
      File rootDir = this.getRootProjectDirectory(currentDir);
      System.out.println("root dir:" + rootDir.getCanonicalPath());
      List<File> ymlFileList = this.matchChildFileExtension(rootDir);
      List<ServerInfo> serverInfoList = this.getSpringServerInfoList(ymlFileList);
      System.out.println(serverInfoList);
      this.writeServerInfoEnum(serverInfoList);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private File getRootProjectDirectory(final File currentDir) throws IOException {
    final String targetRootDirName = "pbear-root";
    System.out.println("get parent dir from: " + currentDir.getCanonicalPath() + ", target: " + targetRootDirName);

    return this.matchTargetParentFile(currentDir, targetRootDirName)
        .orElseGet(() -> {
          System.out.println("fail to match: " + targetRootDirName);
          return currentDir;
        });
  }

  private Optional<File> matchTargetParentFile(final File file, final String targetFileName) {
    File parent = file.getParentFile();
    if (parent == null || file.getName().equalsIgnoreCase(parent.getName())) {
      return Optional.empty();
    }
    if (targetFileName.equalsIgnoreCase(parent.getName())) {
      return Optional.of(parent);
    }
    return this.matchTargetParentFile(parent, targetFileName);
  }

  private List<File> matchChildFileExtension(final File rootDir) throws IOException {
    List<File> resultList = Collections.emptyList();
    if (!rootDir.isDirectory()) {
      return Collections.emptyList();
    }
    try(Stream<Path> walk = Files.walk(rootDir.toPath())) {
      return walk
          .filter(path -> !Files.isDirectory(path))
          .filter(path -> path.toString().endsWith("yml"))
          .peek(path -> System.out.println("file match! >> " + path))
          .map(Path::toFile)
          .collect(Collectors.toList());
    }
  }

  private List<ServerInfo> getSpringServerInfoList(final List<File> ymlFileList) {
    return ymlFileList
        .stream()
        .map(this::parseToYamlmap)
        .map(yamlMap -> {
          try {
            String appName = String.valueOf(this.getValue(yamlMap, "spring", "application", "name").orElseThrow());
            String basePath = String.valueOf(this.getValue(yamlMap, "spring", "webflux", "base-path").orElse("/"));
            return new ServerInfo(appName, basePath);
          } catch (Exception e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .distinct()
        .toList();
  }

  private Map<String, Object> parseToYamlmap(final File yamlFile) {
    try {
      Yaml yaml = new Yaml();
      return yaml.load(new FileInputStream(yamlFile));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private Optional<Object> getValue(final Map<String, Object> source, final String... keys) {
    Map<?,?> tempMap = source;
    for (int i=0; i<keys.length; i++ ) {
      Object tempValue = tempMap.get(keys[i]);
      if (i == keys.length - 1) {
        return Optional.of(tempValue);
      }
      if (tempValue instanceof Map<?,?> x) {
        tempMap = x;
      } else {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  private void writeServerInfoEnum(final List<ServerInfo> serverInfoList) throws IOException {
    FileObject fileObject = processingEnv
        .getFiler()
        .createSourceFile(SERVER_ENUM_PACKAGE_NAME + "." + SERVER_ENUM_CLASS_NAME);
    try (PrintWriter out = new PrintWriter(fileObject.openWriter())) {
      // package com.pbear.devtool;
      out.print("package ");
      out.print(SERVER_ENUM_PACKAGE_NAME);
      out.println(";");
      out.println();

      // import ...
      out.println("import java.util.Arrays;");
      out.println("import java.util.Map;");
      out.println("import java.util.stream.Collectors;");

      // public enum Server {
      out.print("public enum ");
      out.print(SERVER_ENUM_CLASS_NAME);
      out.println(" {");

      for (int i=0; i<serverInfoList.size(); i++) {
        ServerInfo serverInfo = serverInfoList.get(i);
        String enumVarName = serverInfo.applicationName
            .replaceAll("-", "_")
            .replaceAll(" ", "_")
            .replaceAll("\\.", "_")
            .toUpperCase();

        // SERVER_NAME("applicationName", "basePath")
        out.print(TAB + enumVarName);
        out.print("(\"");
        out.print(serverInfo.applicationName);
        out.print("\", \"");
        out.print(serverInfo.basePath);
        out.print("\")");
        if (i == serverInfoList.size() -1) {
          out.println(";");
        } else {
          out.println(",");
        }
      }
      out.println();

      this.printStaticEnumGetter(out);

      this.printEnumBodyCode(out, "applicationName", "basePath");

      out.println("}");
    }
  }

  private void printStaticEnumGetter(final PrintWriter out) {
    out.println(TAB + "private static final Map<String, Server> ENUM_MAP = " +
        "Arrays.stream(Server.values()).collect(Collectors.toUnmodifiableMap(Server::getApplicationName, e -> e));");
    out.println();
    out.println(TAB + "public static Server from(String applicationName) {");
    out.println(TAB + TAB + "return ENUM_MAP.get(applicationName);");
    out.println(TAB + "}");
    out.println();
  }

  private void printEnumBodyCode(final PrintWriter out, final String... args) {
    // member variables
    this.printMemberVariables(out, args);

    // constructor
    this.printConstructor(out, args);

    // getters
    this.printGetters(out, args);
  }

  private void printMemberVariables(final PrintWriter out, final String... args) {
    for (String arg : args) {
      out.println(TAB + "private final String " + arg + ";");
    }
    out.println();
  }

  private void printConstructor(final PrintWriter out, final String... args) {
    out.print(TAB + "Server(");
    for (int i=0; i<args.length; i++) {
      if (i != 0) {
        out.print(", ");
      }
      out.print("final String " + args[i]);
    }
    out.println(") {");
    for (String arg : args) {
      out.println(TAB + TAB + "this." + arg + " = " + arg + ";");
    }
    out.println(TAB + "}");
    out.println();
  }

  private void printGetters(final PrintWriter out, final String... args) {
    for (String arg : args) {
      out.println(TAB + "public String get" + arg.substring(0, 1).toUpperCase() + arg.substring(1) + "() {");
      out.println(TAB + TAB + "return this." + arg + ";");
      out.println(TAB + "}");
      out.println();
    }
  }

//  sample of create resource
//  private void writeServerInfoProperties(final List<ServerInfo> serverInfoList) throws IOException {
//    for (ServerInfo serverInfo : serverInfoList) {
//      FileObject fileObject = processingEnv
//          .getFiler()
//          .createResource(
//              StandardLocation.SOURCE_OUTPUT,
//              "resources",
//              "server-info.properties");
//      try (PrintWriter out = new PrintWriter(fileObject.openWriter())) {
//        serverInfo.toProperties()
//            .store(out, "generated by " + this.getClass().getName());
//      }
//    }
//  }

  record ServerInfo(String applicationName, String basePath) {
    @Override
    public boolean equals(final Object o) {
      if (o == null) {
        return false;
      }
      if (o instanceof ServerInfo y) {
        return this.applicationName.equals(y.applicationName());
      }
      return false;
    }
  }
}
