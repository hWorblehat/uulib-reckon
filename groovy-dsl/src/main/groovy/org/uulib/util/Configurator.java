package org.uulib.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.uulib.util.function.ExceptionalFunction;

import groovy.lang.Closure;

public interface Configurator<C> {
	
	static <C> Configurator<C> using(Closure<?> closure) {
		return new ClosureConfigurator<>(closure);
	}
	
	static <C> Configurator<C> using(Consumer<? super C> consumer) {
		return new ConsumerConfigurator<>(consumer);
	}
	
	<T,E extends Exception> T withConfigured(Supplier<C> configCreator, ExceptionalFunction<C,T,E> action) throws E;
	
	static class ClosureConfigurator<C> implements Configurator<C> {
		private final Closure<?> closure;
		
		private ClosureConfigurator(Closure<?> closure) {
			this.closure = closure;
		}

		@Override
		public <T,E extends Exception> T withConfigured(Supplier<C> configCreator, ExceptionalFunction<C,T,E> action)
				throws E {
			C config = configCreator.get();
			Object oldDelegate = closure.getDelegate();
			try {
				closure.setDelegate(config);
				closure.setResolveStrategy(Closure.DELEGATE_FIRST);
				closure.call();
				
				return action.apply(config);
			} finally {
				closure.setDelegate(oldDelegate);
			}
		}
		
	}
	
	static class ConsumerConfigurator<C> implements Configurator<C> {
		private final Consumer<? super C> consumer;
		
		private ConsumerConfigurator(Consumer<? super C> consumer) {
			this.consumer = consumer;
		}

		@Override
		public <T,E extends Exception> T withConfigured(Supplier<C> configCreator, ExceptionalFunction<C,T,E> action)
				throws E {
			C config = configCreator.get();
			consumer.accept(config);
			return action.apply(config);
		}
		
		
	}
	
}
