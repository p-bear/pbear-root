package com.pbear.subway.business.core.data.mapper;

import com.pbear.subway.business.collect.data.dto.ResSubwayStationMaster;
import com.pbear.subway.business.core.document.Station;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StationMapper {
  StationMapper INSTANCE = Mappers.getMapper(StationMapper.class);

  @Mapping(source = "statnId", target = "id")
  @Mapping(source = "statnNm", target = "name")
  @Mapping(source = "route", target = "routeName")
  @Mapping(source = "crdntX", target = "latitude")
  @Mapping(source = "crdntY", target = "longitude")
  Station toStationDocument(final ResSubwayStationMaster.Station station);
}
