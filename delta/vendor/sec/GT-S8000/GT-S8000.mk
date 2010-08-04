PRODUCT_PACKAGES := \
    IM \
    VoiceDialer \
    MediaPlayer \
    Development \
    SpareParts \
    PackageInstaller \
    TsCalibrate \
    Term

$(call inherit-product, build/target/product/generic.mk)

# Overrides
PRODUCT_BRAND := GT-S8000
PRODUCT_DEVICE := GT-S8000
PRODUCT_NAME := GT-S8000

