PRODUCT_PACKAGES := \
    IM \
    VoiceDialer \
    MediaPlayer \
    Development \
    SpareParts \
    Terminal \

$(call inherit-product, build/target/product/generic.mk)

# Overrides
PRODUCT_BRAND := GT-S8000
PRODUCT_DEVICE := GT-S8000
PRODUCT_NAME := GT-S8000

