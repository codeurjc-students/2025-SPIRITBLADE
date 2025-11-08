# 🔐 Credentials for Development and Demo

## Default Users

The system creates two users by default in **development mode**:

### 👤 Regular User
- **Username:** `user`
- **Password:** `pass`
- **Email:** `user@example.com`
- **Role:** `USER`
- **Access:** Dashboard, Summoner Search, Profile

### 🛡️ Administrator
- **Username:** `admin`
- **Password:** `admin`
- **Email:** `admin@example.com`
- **Role:** `ADMIN`
- **Access:** Admin Panel (User Management)

## ⚠️ Important Access Restrictions

### Admin Limitations
- **Admins CANNOT access user features** (Dashboard, Summoner Search, etc.)
- Admins are restricted to the Admin Panel only
- **If an admin wants to use regular user features, they must login with a regular user account**

### Reasoning
This separation ensures:
- Clear role boundaries
- Better security (admins can't accidentally perform user actions)
- Encourages proper account management
- Follows principle of least privilege

### Example Use Case
If an administrator wants to:
1. Manage users → Login as `admin`
2. Search summoners or use dashboard → Login as `user` (or create a separate user account)

