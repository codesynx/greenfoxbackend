# 🦊 GreenFox

**Resort & Travel Booking Service for Kazakhstan**

A comprehensive backend service for booking and monitoring resort areas, recreational zones, and tourist locations in Kazakhstan.

## 🏗️ Architecture

```
backend/
├── src/main/java/com/greenfox/backend/
│   ├── config/              # Application configurations
│   ├── security/            # JWT, Auth filters, Guards
│   ├── common/              # Shared utilities
│   │   ├── dto/             # ApiResponse, PageResponse
│   │   ├── entity/          # BaseEntity
│   │   ├── exception/       # Global exception handling
│   │   └── service/         # StorageService
│   └── modules/
│       ├── auth/            # OTP authentication, Twilio
│       ├── user/            # User profiles
│       ├── resort/          # Resort listings
│       ├── promo/           # Promotions
│       ├── booking/         # Core booking logic
│       ├── notification/    # Push notifications
│       └── support/         # Chat/Tickets
```

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.4
- **Language**: Java 21
- **Database**: PostgreSQL 16
- **Cache/OTP**: Redis
- **Storage**: GCP Cloud Storage
- **SMS**: Twilio
- **Payment**: Kaspi (DeepLink)
- **Documentation**: OpenAPI/Swagger

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+

### 1. Start Infrastructure

```bash
# Start PostgreSQL and Redis
docker-compose up -d
```

### 2. Configure Application

Update `backend/src/main/resources/application.properties` with your credentials:

```properties
# Twilio (for OTP)
app.twilio.account-sid=YOUR_TWILIO_SID
app.twilio.auth-token=YOUR_TWILIO_TOKEN
app.twilio.phone-number=+1234567890

# GCP Storage (for photos)
app.gcp.project-id=YOUR_GCP_PROJECT
app.gcp.bucket-name=YOUR_BUCKET_NAME

# JWT Secret (change in production!)
app.jwt.secret=your-256-bit-secret-key-minimum-32-characters
```

### 3. Run Application

```bash
cd backend
./mvnw spring-boot:run
```

### 4. Access API

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## 📚 API Endpoints

### 🔐 Authentication (`/api/v1/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/send-otp` | Send OTP to phone |
| POST | `/verify-otp` | Verify OTP & login |
| POST | `/resend-otp` | Resend OTP |
| POST | `/refresh` | Refresh tokens |

### 👤 Users (`/api/v1/users`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/profile` | Get profile |
| PATCH | `/profile` | Update profile |
| POST | `/device-token` | Save FCM token |

### 🏨 Resorts (`/api/v1/resorts`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List resorts (paginated, filtered) |
| GET | `/{id}` | Get resort details |
| GET | `/cities` | Get all cities |

### 🎉 Promos (`/api/v1/promos`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get active promos |

### 📅 Bookings (`/api/v1/bookings`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/calc` | Calculate price |
| POST | `/` | Create booking |
| POST | `/{id}/pay` | Confirm payment |
| GET | `/my` | Get my bookings |
| GET | `/{id}` | Get booking details |

### 🔔 Notifications (`/api/v1/notifications`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get notifications |
| GET | `/unread-count` | Get unread count |
| POST | `/mark-all-read` | Mark all as read |

### 💬 Support (`/api/v1/support`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/message` | Send message |
| GET | `/history` | Get chat history |

### 🔧 Admin Endpoints (`/api/v1/admin/*`)

Requires `ADMIN` role:

- **Resorts**: CRUD operations, photo management
- **Promos**: Create/delete promotions
- **Bookings**: View all, update status
- **Support**: View conversations, reply

## 🔒 Roles

| Role | Access |
|------|--------|
| `USER` | Profile, bookings, support chat |
| `ADMIN` | Full access to all modules |

## 📊 Booking Flow

```
PENDING → PAID_WAITING → CONFIRMED → COMPLETED
    ↓          ↓            ↓
 CANCELLED  CANCELLED   CANCELLED
```

1. **PENDING**: Booking created, awaiting payment
2. **PAID_WAITING**: Payment confirmed, awaiting admin approval
3. **CONFIRMED**: Admin confirmed booking
4. **COMPLETED**: Stay completed
5. **CANCELLED**: Booking cancelled

## 🗄️ Database Schema

### Core Entities
- `users` - User accounts (phone-based auth)
- `resorts` - Resort listings with JSONB amenities/photos
- `promos` - Promotional discounts
- `bookings` - Reservations with guest info
- `notifications` - User notifications
- `support_messages` - Support chat messages

## 🧪 Development

```bash
# Run tests
./mvnw test

# Build JAR
./mvnw clean package

# Run with profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📝 Environment Variables

| Variable | Description |
|----------|-------------|
| `TWILIO_ACCOUNT_SID` | Twilio Account SID |
| `TWILIO_AUTH_TOKEN` | Twilio Auth Token |
| `TWILIO_PHONE_NUMBER` | Twilio Phone Number |
| `GCP_PROJECT_ID` | GCP Project ID (e.g., `my-project-12345`) |
| `GCP_BUCKET_NAME` | GCP Storage Bucket name |
| `GOOGLE_APPLICATION_CREDENTIALS` | Path to GCP service account JSON key file (for local development) |
| `GCP_SERVICE_ACCOUNT_JSON` | Full JSON content of service account key (for cloud deployments like Render) |

### 🔧 GCP Cloud Storage Setup

1. **Create a GCP Project** and enable Cloud Storage API
2. **Create a Storage Bucket**: 
   ```bash
   gsutil mb -p YOUR_PROJECT_ID gs://greenfox-storage
   ```
3. **Make bucket publicly readable** (for image URLs):
   
   **Option A: Using gsutil (recommended)**:
   ```bash
   # Enable uniform bucket-level access (recommended for new buckets)
   gsutil uniformbucketlevelaccess set on gs://greenfox-storage
   
   # Make bucket publicly readable
   gsutil iam ch allUsers:objectViewer gs://greenfox-storage
   ```
   
   **Option B: Using GCP Console**:
   1. Go to Cloud Storage → Buckets → Select your bucket
   2. Click "Permissions" tab
   3. Click "Grant Access"
   4. Add principal: `allUsers`
   5. Select role: `Storage Object Viewer`
   6. Save
   
   **Note**: The backend automatically sets public ACL on uploaded files. However, you must ensure the bucket itself allows public access for the URLs to work without authentication.
4. **Create Service Account** and download JSON key:
   - Go to IAM & Admin > Service Accounts
   - Create service account with "Storage Admin" role
   - Download JSON key file
5. **For Local Development** - Set environment variable:
   ```bash
   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json
   ```
   
6. **For Render Deployment**:
   - Open your Render dashboard → Your Web Service → Environment
   - Add a new environment variable:
     - **Key**: `GCP_SERVICE_ACCOUNT_JSON`
     - **Value**: Paste the entire contents of your JSON key file (all text from `{` to `}`)
   - Render supports multiline values, so you can paste the entire JSON
   - Also set:
     - `GCP_PROJECT_ID` = `greenfox-481812` (or your project ID)
     - `GCP_BUCKET_NAME` = `greenfox-storage` (or your bucket name)
   - Save and redeploy your service
   
   **Important**: The application will automatically use `GCP_SERVICE_ACCOUNT_JSON` if available (for cloud), otherwise it falls back to `GOOGLE_APPLICATION_CREDENTIALS` (file path, for local development).

## 🚧 TODO

- [ ] FCM push notification integration
- [ ] Kaspi webhook for real payment verification
- [ ] Rate limiting for OTP requests
- [ ] Admin dashboard statistics
- [ ] Email notifications


Proprietary - GreenFox ©

