# Build & Development Commands

- Build project: `mvn clean install`
- Run application: `mvn spring-boot:run`
- Package executable: `mvn package`
- Code coverage: `mvn cobertura:cobertura`

# Code Style Guidelines

## Java Conventions
- Indentation: 4 spaces, no tabs
- Line wrapping: 120 characters max
- Braces: Opening brace on same line, closing on new line
- Naming: PascalCase for classes, camelCase for methods/variables, UPPER_SNAKE_CASE for constants

## Import Organization
- Java/javax packages first
- Third-party libraries second (alphabetically)
- Project imports last (com.superluli.*)
- No wildcard imports

## Best Practices
- Use SLF4J for logging with appropriate levels
- Include request IDs in logs for traceability
- Create custom exceptions extending Spring's NestedRuntimeException
- Handle exceptions properly with centralized error handling
- Document public APIs with JavaDoc