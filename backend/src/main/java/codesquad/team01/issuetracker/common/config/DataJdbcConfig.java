package codesquad.team01.issuetracker.common.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;

@Configuration
@EnableJdbcAuditing
public class DataJdbcConfig {

	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> Optional.of("system");
	}

	@Bean
	public NamingStrategy namingStrategy() {
		return new NamingStrategy() {
			@Override
			public String getTableName(Class<?> type) {
				return toSnake(type.getSimpleName());
			}

			@Override
			public String getSchema() {
				return "";
			}

			@Override
			public String getColumnName(RelationalPersistentProperty property) {
				return toSnake(property.getName());
			}

			private String toSnake(String input) {
				return input.replaceAll("([a-z])([A-Z])", "$1_$2")
					.toLowerCase();
			}
		};
	}
}
