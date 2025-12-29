# 🖨️ Kueue

**Kueue** is a **cross-platform thermal printer simulator** built with **Kotlin Multiplatform**.

It behaves like a **real ESC/POS thermal printer** over TCP:

- Listens on an IP + port
- Accepts raw ESC/POS bytes
- Supports **multiple print jobs over the same connection**
- Decodes **text and raster images**
- Splits jobs using **paper cut commands**
- Plays a sound when a receipt is printed

Supported platforms:

- ✅ Android
- ✅ iOS
- ✅ JVM / Desktop (Compose)

