# Lantify

**Lantify** is a lightweight and user-friendly LAN file transfer application built with **Java** and **JavaFX**.  
It enables seamless file sharing between two computers on the same local network without requiring complex setup or internet access.

## Features

- 📂 **Multiple File Selection** – Send one or more files in a single transfer.
- 📊 **Progress Bar** – Real-time progress tracking during transfers.
- ⚡ **Fast Transfer Speeds** – Optimized for local network performance.
- 📄 **File Information Preview** – Displays file name and size before transfer.
- 🔒 **Direct LAN Connection** – No third-party servers or internet dependency.
- 🖥 **Cross-Platform** – Runs on Windows, macOS, and Linux with Java support.

## How It Works

1. **Receiver** starts the application, selects the save directory, and listens on a chosen port.
2. **Sender** opens the application, selects files, enters the receiver's IP and port, then sends.
3. Files are transferred directly over the local network.

## Requirements

- **Java 17+**
- Local network connection between sender and receiver.
- Open port for file transfer (default: `5000`).

## License

This project is released under the [MIT License](LICENSE).
