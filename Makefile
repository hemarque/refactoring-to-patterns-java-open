default: docker-tests

# Local commands
.PHONY: tests
tests:
	gradle :test

# Docker commands
docker-build:
	@docker build -t codiumteam/refactoring-to-patterns-java .

docker-tests:
	@docker run --rm -v ${PWD}:/code -v ${PWD}/.gradle:/gradle-cache codiumteam/refactoring-to-patterns-java make tests
