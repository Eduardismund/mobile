# ExamPrep - Recipe Manager Setup Guide

This guide explains how to set up and run the Recipe Manager Android app with the Node.js server running in WSL.

## Project Structure

```
ExamPrep/
├── app/                    # Android application (Kotlin)
├── server/                 # Node.js server (Koa.js)
└── SETUP.md               # This file
```

## Network Configuration

### Your Network Setup
- **Windows PC IP:** `192.168.1.194` (on Wi-Fi)
- **WSL Internal IP:** `172.23.137.190` (changes on reboot)
- **Phone:** Connected to same Wi-Fi network (192.168.1.x)

### How It Works
```
Phone (192.168.1.xxx)
    ↓
Windows PC (192.168.1.194:2528)
    ↓ [Port Proxy]
WSL (172.23.137.190:2528)
    ↓
Node.js Server
```

## Initial Setup (One Time)

### 1. Windows Firewall Rule
Open **PowerShell as Administrator** and run:

```powershell
```

This allows incoming connections on port 2528.netsh advfirewall firewall add rule name="Node Server Port 2506" dir=in action=allow protocol=TCP localport=2506


### 2. Port Proxy Configuration
In **PowerShell as Administrator**, run:

```powershell
netsh interface portproxy add v4tov4 listenport=2506 listenaddress=0.0.0.0 connectport=2506 connectaddress=172.23.137.190
```

This forwards traffic from Windows (192.168.1.194:2528) to WSL (172.23.137.190:2528).

**Verify it was added:**
```powershell
netsh interface portproxy show all
```

You should see:
```
Listen on ipv4:             Connect to ipv4:

Address         Port        Address         Port
--------------- ----------  --------------- ----------
0.0.0.0         2528        172.23.137.190  2528
```

## Starting the Server

### Option 1: From WSL (Current Setup)

1. Navigate to server directory:
   ```bash
   cd /mnt/c/Users/Eduard/AndroidStudioProjects/ExamPrep/server
   ```

2. Install dependencies (first time only):
   ```bash
   npm install
   ```

3. Start the server:
   ```bash
   npm start
   ```

You should see:
```
🚀 Server listening on 2528 ... 🚀
```

### Option 2: Using Make
```bash
cd /mnt/c/Users/Eduard/AndroidStudioProjects/ExamPrep/server
make
```

## Android App Configuration

The app is configured to connect to: `http://192.168.1.194:2528/`

This is set in: `app/src/main/java/com/example/examprep/network/NetworkConfig.kt`

```kotlin
private const val SERVER_IP = "192.168.1.194"
private const val SERVER_PORT = "2528"
```

## Running the App

1. **Start the server** (see above)
2. **Open Android Studio**
3. **Build → Rebuild Project**
4. **Run** the app on your physical device
5. The app will fetch and display recipes from the server

## Testing the Connection

### Test from WSL
```bash
curl http://172.23.137.190:2528/recipes
```

### Test from Windows
```powershell
Invoke-WebRequest -Uri "http://192.168.1.194:2528/recipes"
```

### Test from Phone
Open browser on phone and navigate to:
```
http://192.168.1.194:2528/recipes
```

You should see JSON data with 10 recipes.

## Troubleshooting

### Server not accessible from phone

1. **Check if server is running:**
   ```bash
   netstat -an | grep 2528
   ```
   Should show: `tcp6  0  0 :::2528  :::*  LISTEN`

2. **Check firewall rule:**
   ```powershell
   netsh advfirewall firewall show rule name="Node Server Port 2528"
   ```

3. **Check port proxy:**
   ```powershell
   netsh interface portproxy show all
   ```
   Make sure port 2528 is listed.

4. **Check WSL IP hasn't changed:**
   ```bash
   ip addr show eth0 | grep "inet "
   ```
   If the IP changed from `172.23.137.190`, update the port proxy:
   ```powershell
   # Delete old rule
   netsh interface portproxy delete v4tov4 listenport=2528 listenaddress=0.0.0.0

   # Add new rule with updated IP
   netsh interface portproxy add v4tov4 listenport=2528 listenaddress=0.0.0.0 connectport=2528 connectaddress=<NEW_WSL_IP>
   ```

### App shows "Network error"

1. **Verify server is running** (see above)
2. **Check phone is on same Wi-Fi** as computer
3. **Rebuild the app** in Android Studio
4. **Check Android app logs** in Logcat for connection errors

### WSL IP Changed After Reboot

The WSL IP (`172.23.137.190`) can change when you restart Windows. If this happens:

1. Get new WSL IP:
   ```bash
   ip addr show eth0 | grep "inet " | awk '{print $2}' | cut -d/ -f1
   ```

2. Update port proxy:
   ```powershell
   # Delete old rule
   netsh interface portproxy delete v4tov4 listenport=2528 listenaddress=0.0.0.0

   # Add with new IP
   netsh interface portproxy add v4tov4 listenport=2528 listenaddress=0.0.0.0 connectport=2528 connectaddress=<NEW_IP>
   ```

## Cleanup Commands

### Remove Firewall Rule
```powershell
netsh advfirewall firewall delete rule name="Node Server Port 2528"
```

### Remove Port Proxy
```powershell
netsh interface portproxy delete v4tov4 listenport=2528 listenaddress=0.0.0.0
```

## API Endpoints

- `GET /recipes` - Get all recipes
- `GET /recipe/:id` - Get recipe by ID
- `POST /recipe` - Create new recipe (requires: date, title, ingredients, category, rating)
- `DELETE /recipe/:id` - Delete recipe by ID
- `GET /allRecipes` - Get all recipes (alternative endpoint)

## Server Details

- **Framework:** Koa.js (Node.js)
- **Port:** 2528
- **WebSocket:** Enabled (broadcasts new recipes)
- **CORS:** Enabled
- **Sample Data:** 10 pre-loaded recipes

## Android App Details

- **Architecture:** MVVM (Model-View-ViewModel)
- **Language:** Kotlin
- **Networking:** Retrofit + OkHttp
- **UI:** Material Design with RecyclerView
- **Features:**
  - Display all recipes in a list
  - Delete recipes with confirmation
  - Loading indicators
  - Error handling

## Important Notes

⚠️ **Do NOT modify server code** - The server must remain exactly as it is from the GitHub repository.

⚠️ **Port proxy required** - Without the port proxy, your phone cannot reach the WSL server.

⚠️ **Same Wi-Fi network** - Phone and computer must be on the same network.

⚠️ **WSL IP can change** - After restarting Windows, you may need to update the port proxy with the new WSL IP.
