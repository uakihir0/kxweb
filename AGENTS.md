# Agent Documentation

## Overview

This repository is an API client library for X (Twitter) web (Kotlin Multiplatform compatible). This library interacts with X (Twitter)'s unofficial/undocumented web APIs.

**Note:** This project is currently in early development. The implementation is strongly inspired by [Nitter](https://github.com/zedeus/nitter) and [bird](https://github.com/steipete/bird), which provide valuable insights into Twitter/X web API interactions.

## Key References

### Nitter

[Nitter](https://github.com/zedeus/nitter) is a privacy-focused alternative Twitter frontend that:
- Handles all requests server-side (client never contacts Twitter directly)
- Prevents IP-based tracking and browser fingerprinting
- Uses Twitter's unofficial API without requiring developer accounts
- Provides lightweight page delivery

**Key insights from Nitter:**
- Privacy-focused request handling patterns
- Server-side API interaction approaches
- Lightweight data processing techniques

### bird

[bird](https://github.com/steipete/bird) is a CLI tool that:
- Uses X/Twitter's undocumented web GraphQL API
- Provides reading and querying capabilities
- Implements cookie-based authentication from web browsers
- Handles endpoint discovery and query ID management

**Key insights from bird:**
- GraphQL API endpoint patterns
- Authentication and session management
- Query structure and parameter handling

## Important Concepts

### X (Twitter) Web API Structure

X (Twitter) web APIs are primarily GraphQL-based with the following characteristics:

- **GraphQL Endpoints**: `/i/api/graphql/{queryId}/{operationName}`
- **Authentication**: Cookie-based or Bearer token authentication
- **Rate Limiting**: Per-endpoint rate limits enforced
- **API Stability**: Endpoints, query IDs, and anti-bot behavior can change at any time

### API Categories (Planned)

- **Authentication**: Login, session management, token handling
- **Timeline Operations**: Home timeline, user timeline, list timeline
- **Tweet Operations**: Create, delete, like, retweet, reply
- **User Operations**: Profile information, followers, following
- **Search**: Tweet search, user search, hashtag search
- **Direct Messages**: DM conversations and messaging

## Directory Structure (Planned)

Once implementation begins, the structure will follow this pattern:

- **`core/`**: Core library for X (Twitter) web API
  - `api/` - API resource interfaces
  - `entity/` - Data models (Tweet, User, etc.)
  - `internal/` - Internal implementations
- **`auth/`**: Authentication functionality
- **`stream/`**: Real-time streaming (if applicable)
- **`all/`**: Package containing all modules
- **`plugins/`**: Gradle build configuration

## Implementation Guidelines

### Referencing Source Projects

When implementing features, refer to:

1. **Nitter source code**: Privacy patterns and server-side request handling
   - Repository: https://github.com/zedeus/nitter
   - Key files: API implementation, authentication, data models

2. **bird source code**: GraphQL API patterns and endpoint structures
   - Repository: https://github.com/steipete/bird
   - Key files: API clients, authentication, query builders

### API Stability Considerations

- X (Twitter) APIs are undocumented and can change without notice
- Implement flexible error handling and fallback mechanisms
- Monitor for API changes and breaking updates
- Consider rate limiting and anti-bot detection

### Testing (Future)

Once implementation begins, testing will include:

```shell
# Run all tests
./gradlew :core:jvmTest

# Run specific test
./gradlew :core:jvmTest --tests "work.socialhub.kxweb.TwitterTest"

# Verify build
./gradlew jvmJar
```

Authentication credentials for testing will be managed via `secrets.json` (similar to other projects in the ecosystem).

## Development Roadmap

1. **Phase 1**: Basic API structure and authentication
2. **Phase 2**: Timeline and tweet operations
3. **Phase 3**: User operations and search
4. **Phase 4**: Advanced features (DMs, streaming)

## Important Notes

- **Privacy Focus**: Design should follow privacy-focused patterns inspired by Nitter
- **API Discovery**: Use bird's approach for discovering and handling GraphQL endpoints
- **Rate Limiting**: Implement rate limit tracking per endpoint
- **Error Handling**: Robust error handling for API changes and failures
- **Platform Compatibility**: Follow Kotlin Multiplatform and khttpclient patterns

## Contributing

When contributing to this project:

1. Study Nitter and bird implementations for reference
2. Test against real X (Twitter) APIs carefully (be mindful of rate limits)
3. Document API endpoint structures and parameters
4. Implement comprehensive error handling
5. Follow the existing code patterns from kbsky, kmastodon, and kmisskey

## Key File References (Future)

Once implementation begins:

| Purpose                  | File Path (Planned)                                                 |
| ------------------------ | ------------------------------------------------------------------- |
| API endpoint definitions | `core/src/commonMain/kotlin/work/socialhub/kxweb/XWebAPI.kt`       |
| Main interface           | `core/src/commonMain/kotlin/work/socialhub/kxweb/XWeb.kt`          |
| Factory                  | `core/src/commonMain/kotlin/work/socialhub/kxweb/XWebFactory.kt`   |
| API usage examples       | `core/src/jvmTest/kotlin/work/socialhub/kxweb/apis/`               |
| Authentication           | `auth/src/commonMain/kotlin/work/socialhub/kxweb/auth/`            |
