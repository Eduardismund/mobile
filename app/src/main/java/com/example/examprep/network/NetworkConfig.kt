package com.example.examprep.network

object NetworkConfig {
    /**
     * Server Configuration
     *
     * IMPORTANT: Update the SERVER_IP based on your setup:
     *
     * - For Android Emulator: use "10.0.2.2"
     * - For Physical Device: use your computer's local IP address
     *   (e.g., "192.168.1.194" or whatever your computer's IP is on your local network)
     *
     * To find your computer's IP:
     * - Windows: Open Command Prompt and run "ipconfig", look for "IPv4 Address"
     * - Mac/Linux: Open Terminal and run "ifconfig" or "ip addr"
     * - Look for addresses starting with 192.168.x.x or 10.x.x.x
     */

    // Current IP - Windows network IP (port proxy forwards to WSL)
    private const val SERVER_IP = "192.168.1.194"

    // Server port (don't change this)
    private const val SERVER_PORT = "2528"

    // Full base URL
    const val BASE_URL = "http://$SERVER_IP:$SERVER_PORT/"

    /**
     * Quick toggle between emulator and physical device
     * Uncomment the one you need:
     */
    // For Emulator:
    // const val BASE_URL = "http://10.0.2.2:2528/"

    // For Physical Device (current):
    // const val BASE_URL = "http://192.168.1.194:2528/"
}
