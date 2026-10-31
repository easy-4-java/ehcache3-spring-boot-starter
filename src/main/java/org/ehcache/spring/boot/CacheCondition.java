package org.ehcache.spring.boot;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;

/**
 * General cache condition used with all cache configuration classes.
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 1.3.0
 */
public class CacheCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context,
			AnnotatedTypeMetadata metadata) {
		String sourceClass = "";
		if (metadata instanceof ClassMetadata) {
			sourceClass = ((ClassMetadata) metadata).getClassName();
		}
		ConditionMessage.Builder message = ConditionMessage.forCondition("Cache",
				sourceClass);
		String value = Binder.get(context.getEnvironment()).bind("spring.cache.type", String.class).orElse(null);
		if (value == null) {
			return ConditionOutcome.match(message.because("automatic cache type"));
		}
		CacheType cacheType = CacheConfigurations
				.getType(((AnnotationMetadata) metadata).getClassName());
		value = value.replace('-', '_').toUpperCase(java.util.Locale.ROOT);
		if (value.equals(cacheType.name())) {
			return ConditionOutcome.match(message.because(value + " cache type"));
		}
		return ConditionOutcome.noMatch(message.because(value + " cache type"));
	}

}

