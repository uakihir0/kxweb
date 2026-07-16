build:
	./gradlew \
	core:clean \
	core:assemble \
	-x test --refresh-dependencies

pods:
	./gradlew \
	all:assembleKxwebXCFramework \
  	all:podPublishXCFramework \
	-x test --refresh-dependencies

version:
	 ./gradlew version --no-daemon --console=plain -q

.PHONY: build pods version
