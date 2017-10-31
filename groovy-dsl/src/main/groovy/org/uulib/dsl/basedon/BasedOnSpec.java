package org.uulib.dsl.basedon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.uulib.dsl.basedon.BasedOnSpec;
import org.uulib.util.ConstantSupplier;

public final class BasedOnSpec<S, T> implements BasedOn {
	
	Optional<Callable<T>> whenAbsent = Optional.empty();
	Optional<Callable<T>> normal = Optional.empty();
	Map<S, Callable<T>> mappings = new HashMap<>();
	
	BasedOnSpec(){} // package-private
	
	public void normally(Callable<T> normal) {
		this.normal = Optional.of(normal);
	}
	
	public void normally(T normal) {
		normally(ConstantSupplier.nonNull(normal));
	}
	
	public MappingConfig<S,T> when(S val) {
		return new MappingConfig<>(this).or(val);
	}
	
	public MappingConfig<S,T> when(AbsentSingleton absent) {
		return new MappingConfig<>(this).or(absent);
	}
	
	public static class MappingConfig<S,T> {
		private boolean includeAbsent = false;
		private final List<S> mappings = new ArrayList<>();
		private final BasedOnSpec<S,T> parent;
		
		private MappingConfig(BasedOnSpec<S,T> parent) {
			this.parent = parent;
		}
		
		public void then(Callable<T> assoc) {
			Objects.requireNonNull(assoc);
			mappings.forEach(mapping -> parent.mappings.put(mapping, assoc));
			if(includeAbsent) {
				parent.whenAbsent = Optional.of(assoc);
			}
		}
		
		public void then(T assoc) {
			then(ConstantSupplier.nonNull(assoc));
		}
		
		public MappingConfig<S,T> or(S mapping) {
			mappings.add(mapping);
			return this;
		}
		
		public MappingConfig<S,T> or(AbsentSingleton absent) {
			includeAbsent = true;
			return this;
		}
	}

}
