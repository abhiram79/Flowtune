#!/data/data/com.termux/files/usr/bin/bash
set -e

echo "=== Android Build Environment Setup for Termux ==="

# -----------------------------
# 1. Update & base packages
# -----------------------------
echo "[1/7] Installing base packages..."
pkg update -y
pkg install -y openjdk-21 git wget unzip android-tools

# -----------------------------
# 2. Android SDK directories
# -----------------------------
echo "[2/7] Setting up Android SDK directories..."
ANDROID_HOME="$HOME/Android/Sdk"
mkdir -p "$ANDROID_HOME"
cd "$ANDROID_HOME"

# -----------------------------
# 3. Download command-line tools
# -----------------------------
if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
  echo "[3/7] Downloading Android command-line tools..."
  wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
  unzip -q commandlinetools-linux-11076708_latest.zip
  mkdir -p cmdline-tools/latest
  mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
  rm commandlinetools-linux-*.zip
else
  echo "[3/7] Command-line tools already installed."
fi

# -----------------------------
# 4. Environment variables
# -----------------------------
echo "[4/7] Configuring environment variables..."

ENV_BLOCK=$(cat <<'EOF'

# Android SDK
export ANDROID_HOME=$HOME/Android/Sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
EOF
)

for PROFILE in "$HOME/.bashrc" "$HOME/.zshrc"; do
  if [ -f "$PROFILE" ]; then
    if ! grep -q "ANDROID_SDK_ROOT" "$PROFILE"; then
      echo "$ENV_BLOCK" >> "$PROFILE"
      echo "  → Updated $(basename "$PROFILE")"
    else
      echo "  → $(basename "$PROFILE") already configured"
    fi
  else
    echo "$ENV_BLOCK" >> "$PROFILE"
    echo "  → Created and updated $(basename "$PROFILE")"
  fi
done

# Load for current shell
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

# -----------------------------
# 5. Install SDK packages
# -----------------------------
echo "[5/7] Installing Android SDK components..."

yes | sdkmanager --licenses > /dev/null

sdkmanager \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;34.0.0"

# -----------------------------
# 6. Gradle optimization (8GB RAM)
# -----------------------------
echo "[6/7] Optimizing Gradle for Termux..."

mkdir -p "$HOME/.gradle"

cat > "$HOME/.gradle/gradle.properties" <<EOF
org.gradle.daemon=false
org.gradle.jvmargs=-Xmx2048m -XX:+UseG1GC
org.gradle.workers.max=3
org.gradle.parallel=true
kotlin.daemon.jvm.options=-Xmx1024m
EOF

# -----------------------------
# 7. Final verification
# -----------------------------
echo "[7/7] Verifying installation..."

java -version
sdkmanager --version

echo
echo "✅ SETUP COMPLETE"
echo "➡️ Restart Termux or run:"
echo "   source ~/.bashrc  OR  source ~/.zshrc"
echo
echo "➡️ Then inside your project:"
echo "   chmod +x gradlew"
echo "   ./gradlew assembleUniversalFossDebug --no-daemon"
