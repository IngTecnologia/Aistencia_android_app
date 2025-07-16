# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android application for biometric attendance verification using facial recognition and GPS location validation. The app is developed by INEMEC and allows employees to register attendance securely through a two-step verification process.

## Build and Development Commands

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease

# Install debug APK on connected device
./gradlew installDebug

# Clean build
./gradlew clean
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.inemec.verificacionasistencia.ExampleUnitTest"
```

### Development Setup
- Open project in Android Studio
- Configure API URL in `ApiClient.kt` (currently using ngrok tunnel)
- Ensure device has camera and location permissions enabled
- Test on Android 5.0+ (API 21) or higher

## Architecture and Code Structure

### Application Architecture
- **Pattern**: Single Activity with Navigation Component
- **UI**: Fragment-based navigation with 4 main screens
- **Data**: Repository pattern with Retrofit for API communication
- **State Management**: Singleton UserData object for session state

### Key Components
- **MainActivity**: Single activity hosting navigation
- **Fragments**: LoginFragment → LocationFragment → CameraFragment → ResultFragment
- **API Layer**: ApiClient (Retrofit) + ApiService (endpoints)
- **Models**: UserData, InitResponse, VersionResponse
- **UI Components**: FaceGuideOverlay, UpdateDialog, CommentDialog

### Package Structure
```
com.inemec.verificacionasistencia/
├── api/              # API client and service interfaces
├── model/            # Data models and responses
├── ui/               # Fragments and UI components
│   ├── dialogs/      # Custom dialogs
│   └── theme/        # Compose theme (future use)
└── MainActivity.kt   # Main activity
```

### Navigation Flow
1. **LoginFragment**: User enters cedula and selects entry/exit type
2. **LocationFragment**: GPS validation and location verification
3. **CameraFragment**: Facial recognition capture with CameraX
4. **ResultFragment**: Display verification results and attendance confirmation

## API Integration

### Base Configuration
- **Base URL**: Configured in `ApiClient.kt` (currently ngrok tunnel)
- **Authentication**: JWT-like token system with temporary sessions
- **Timeout**: 30 seconds for connect/read/write operations

### Key Endpoints
- `POST /verify-web/init` - Initial validation (location + credentials)
- `POST /verify-web/face` - Facial recognition verification
- `GET /version` - App version checking for updates

### API Response Models
- **InitResponse**: Contains session token and validation status
- **VerifyResponse**: Facial verification results
- **VersionResponse**: Version information and update requirements

## Dependencies and Libraries

### Core Dependencies
- **AndroidX**: Core KTX, AppCompat, Material Design, ConstraintLayout
- **Navigation**: Navigation Component for fragment transitions
- **Retrofit**: REST API client with Gson converter
- **OkHttp**: HTTP client with logging interceptor
- **CameraX**: Camera functionality for facial capture
- **Google Play Services**: Location services for GPS validation

### Version Information
- **Target/Compile SDK**: 35 (Android 15)
- **Min SDK**: 21 (Android 5.0)
- **Java Version**: 11
- **Kotlin JVM Target**: 11

## Security and Permissions

### Required Permissions
- **Internet**: API communication
- **Camera**: Facial recognition capture
- **Fine/Coarse Location**: GPS validation
- **Access Network State**: Network connectivity checking

### Security Features
- HTTPS communication with API
- Temporary session tokens
- Image processing without permanent storage
- Server response validation

## Development Notes

### Current Configuration
- **Environment**: Development with ngrok tunnel
- **API URL**: Must be updated for production deployment
- **Debug Logging**: Enabled for development (should be disabled in production)
- **Proguard**: Currently disabled in release builds

### Key Files to Modify
- `ApiClient.kt`: Update BASE_URL for production
- `app/build.gradle.kts`: Version management and dependencies
- `AndroidManifest.xml`: Permissions and app configuration

### Testing Status
- **Unit Tests**: Minimal (needs expansion)
- **Instrumented Tests**: Basic (needs comprehensive coverage)
- **Manual Testing**: Primary testing method currently used

## Common Tasks

### Adding New Features
1. Create new fragment in `ui/` package
2. Add navigation actions in `navigation.xml`
3. Update API service if backend integration needed
4. Add required permissions in AndroidManifest.xml

### API Changes
1. Update `ApiService.kt` interface
2. Modify/add response models in `model/` package
3. Update `ApiClient.kt` if configuration changes needed
4. Test with updated backend endpoints

### UI Modifications
1. Modify fragment layouts in `res/layout/`
2. Update fragment classes in `ui/` package
3. Ensure Material Design consistency
4. Test on different screen sizes

## Troubleshooting

### Common Issues
- **Network Errors**: Check API URL configuration in ApiClient.kt
- **Permission Errors**: Verify AndroidManifest.xml permissions
- **Camera Issues**: Ensure CameraX dependencies are current
- **Location Errors**: Check Google Play Services configuration

### Debug Tools
- **OkHttp Logging**: Network request/response logging enabled
- **Android Debug Bridge**: Use `adb logcat` for system logs
- **Android Studio Profiler**: For performance analysis