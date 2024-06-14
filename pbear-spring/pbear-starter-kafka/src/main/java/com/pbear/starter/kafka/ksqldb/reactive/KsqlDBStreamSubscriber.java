package com.pbear.starter.kafka.ksqldb.reactive;

import lombok.NonNull;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;

public class KsqlDBStreamSubscriber implements CoreSubscriber<Object>, Disposable {
  static final AtomicReferenceFieldUpdater<KsqlDBStreamSubscriber, Subscription> S =
      AtomicReferenceFieldUpdater.newUpdater(KsqlDBStreamSubscriber.class,
          Subscription.class, "subscription");

  volatile Subscription subscription;
  private final Consumer<Object> consumer;
  private final Consumer<? super Throwable> errorConsumer;
  private final Runnable completeConsumer;
  private final Context initialContext;

  public KsqlDBStreamSubscriber(final Consumer<Object> consumer, final Consumer<? super Throwable> errorConsumer, final Runnable completeConsumer, final Context initialContext) {
    this.consumer = consumer;
    this.errorConsumer = errorConsumer;
    this.completeConsumer = completeConsumer;
    this.initialContext = initialContext == null ? Context.empty() : initialContext;
  }

  @NonNull
  @Override
  public Context currentContext() {
    return this.initialContext;
  }

  @Override
  public void onSubscribe(@NonNull final Subscription subscription) {
    if (Operators.validate(this.subscription, subscription)) {
      this.subscription = subscription;
      subscription.request(1L);
    }

    this.subscription = subscription;
    subscription.request(1);
  }

  @Override
  public void onNext(final Object o) {
    try {
      if (this.consumer != null) {
        this.consumer.accept(o);
      }
      this.subscription.request(1);
    }
    catch (Throwable t) {
      Exceptions.throwIfFatal(t);
      this.subscription.cancel();
      onError(t);
    }
  }

  @Override
  public void onError(final Throwable throwable) {
    Subscription s = S.getAndSet(this, Operators.cancelledSubscription());
    if (s == Operators.cancelledSubscription()) {
      Operators.onErrorDropped(throwable, this.initialContext);
      return;
    }
    if (errorConsumer != null) {
      errorConsumer.accept(throwable);
    }
    else {
      Operators.onErrorDropped(Exceptions.errorCallbackNotImplemented(throwable), this.initialContext);
    }
  }

  @Override
  public void onComplete() {
    Subscription s = S.getAndSet(this, Operators.cancelledSubscription());
    if (s == Operators.cancelledSubscription()) {
      return;
    }
    if (this.completeConsumer != null) {
      try {
        this.completeConsumer.run();
      }
      catch (Throwable t) {
        Exceptions.throwIfFatal(t);
        onError(t);
      }
    }
  }

  @Override
  public void dispose() {
    Subscription s = S.getAndSet(this, Operators.cancelledSubscription());
    if (s != null && s != Operators.cancelledSubscription()) {
      s.cancel();
    }
  }
}
