package com.example.model

enum class AnimationCategory(val label: String) {
    VIRAL_SHORTS("Viral Shorts & TikTok"),
    KINETIC("Kinetic Motion & 3D"),
    DRAMATIC("Dramatic & Impact"),
    GLITCH_CYBER("Glitch & Cyber Tech"),
    SMOOTH_CINEMA("Smooth & Cinematic"),
    RETRO_FUN("Retro, Comic & Fun"),
    CAPCUT_SPECIALS("CapCut Signature FX")
}

enum class AnimationPreset(
    val label: String,
    val category: AnimationCategory = AnimationCategory.VIRAL_SHORTS
) {
    // Viral Shorts & TikTok
    POP_UP("Pop-Up (Hormozi Style)", AnimationCategory.VIRAL_SHORTS),
    BOUNCE("Punch Bounce (MrBeast)", AnimationCategory.VIRAL_SHORTS),
    ZOOM_PUNCH("Crash Zoom Punch", AnimationCategory.VIRAL_SHORTS),
    KARAOKE_POP("Karaoke Highlight Jump", AnimationCategory.VIRAL_SHORTS),
    ELASTIC_DROP("Elastic Spring Drop", AnimationCategory.VIRAL_SHORTS),
    HEARTBEAT_PULSE("Heartbeat Pulse", AnimationCategory.VIRAL_SHORTS),
    SNAP_IN("Instant Snap In", AnimationCategory.VIRAL_SHORTS),
    STOMP_SLAM("Heavy Stomp Slam", AnimationCategory.VIRAL_SHORTS),
    TIKTOK_WIGGLE("TikTok Dynamic Wiggle", AnimationCategory.VIRAL_SHORTS),
    JUMP_LAND("Character Jump & Land", AnimationCategory.VIRAL_SHORTS),

    // Kinetic Motion & 3D
    SLIDE_UP("Slide Up Smooth", AnimationCategory.KINETIC),
    SLIDE_DOWN("Slide Down Drop", AnimationCategory.KINETIC),
    SLIDE_LEFT("Slide In Left", AnimationCategory.KINETIC),
    SLIDE_RIGHT("Slide In Right", AnimationCategory.KINETIC),
    RUBBER_BAND("Rubber Band Stretch", AnimationCategory.KINETIC),
    FLIP_X("3D Flip Horizontal", AnimationCategory.KINETIC),
    FLIP_Y("3D Flip Vertical", AnimationCategory.KINETIC),
    ROLL_IN("Kinetic Barrel Roll", AnimationCategory.KINETIC),
    SKEW_SNAP("Skew Snap Elastic", AnimationCategory.KINETIC),
    ROTATE_IN("360 Spin Drop In", AnimationCategory.KINETIC),
    EXPAND_HORIZ("Horizontal Accordion", AnimationCategory.KINETIC),
    SWING_PENDULUM("Pendulum Clock Swing", AnimationCategory.KINETIC),

    // Dramatic & Impact
    WORD_GLOW("Word Glow Neon Flare", AnimationCategory.DRAMATIC),
    SHAKE_IMPACT("Camera Shake Impact", AnimationCategory.DRAMATIC),
    WAVE_FLOAT("Wave Float Liquid", AnimationCategory.DRAMATIC),
    STROBE_FLASH("Strobe Flash Strike", AnimationCategory.DRAMATIC),
    EXPLOSION("Explosion Shatter", AnimationCategory.DRAMATIC),
    SPIRAL_IN("Vortex Spiral In", AnimationCategory.DRAMATIC),
    LIGHTNING_FLASH("Lightning Bolt Flash", AnimationCategory.DRAMATIC),
    THUNDER_HIT("Thunder Bass Drop Hit", AnimationCategory.DRAMATIC),
    ECHO_REVERB("Visual Echo Wave", AnimationCategory.DRAMATIC),

    // Glitch & Cyber Tech
    TYPEWRITER("Typewriter Terminal Code", AnimationCategory.GLITCH_CYBER),
    GLITCH_FLICKER("Cyber Glitch Flicker", AnimationCategory.GLITCH_CYBER),
    WIGGLE("High Frequency Jitter", AnimationCategory.GLITCH_CYBER),
    SMOKE_DISSOLVE("Digital Dissolve Cloud", AnimationCategory.GLITCH_CYBER),
    MATRIX_RAIN("Matrix Stream Reveal", AnimationCategory.GLITCH_CYBER),
    RGB_SPLIT_MOTION("Chromatic RGB Split", AnimationCategory.GLITCH_CYBER),
    HOLOGRAM_BEAM("Hologram Projector Beam", AnimationCategory.GLITCH_CYBER),
    SCANLINE_REVEAL("CRT Scanline Raster", AnimationCategory.GLITCH_CYBER),

    // Smooth & Cinematic
    FADE_IN("Smooth Dissolve Fade", AnimationCategory.SMOOTH_CINEMA),
    ZOOM_OUT("Slow Cinematic Zoom Out", AnimationCategory.SMOOTH_CINEMA),
    BLUR_IN("Dreamy Defocus Blur In", AnimationCategory.SMOOTH_CINEMA),
    EXPAND_TRACKING("Letter Tracking Expand", AnimationCategory.SMOOTH_CINEMA),
    CINEMA_SUBTITLE_FADE("Subtitle Gentle Cross", AnimationCategory.SMOOTH_CINEMA),
    GLIDE_DIAGONAL("Diagonal Cinematic Glide", AnimationCategory.SMOOTH_CINEMA),
    SOFT_FLOAT("Ambient Zero-G Float", AnimationCategory.SMOOTH_CINEMA),

    // Retro, Comic & Fun
    COMIC_BOOM("Comic BOOM Punch", AnimationCategory.RETRO_FUN),
    ARCADE_BLINK("8-Bit Arcade Blink", AnimationCategory.RETRO_FUN),
    BOBBLE_HEAD("Bobble Head Bounce", AnimationCategory.RETRO_FUN),
    JELLY_SHAKE("Jelly Gelatin Wobble", AnimationCategory.RETRO_FUN),
    RETRO_TAPE_JUMP("VHS Tracking Roll", AnimationCategory.RETRO_FUN),

    // CapCut Signature FX
    CAPCUT_FAST_BOUNCE("CapCut Signature Snap Bounce", AnimationCategory.CAPCUT_SPECIALS),
    CAPCUT_SWIPE_GLOW("CapCut Swipe with Trail Glow", AnimationCategory.CAPCUT_SPECIALS),
    CAPCUT_SPLIT_WORD("CapCut Kinetic Word Pop", AnimationCategory.CAPCUT_SPECIALS),
    CAPCUT_ZOOM_WARP("CapCut 3D Warp Push", AnimationCategory.CAPCUT_SPECIALS)
}

enum class TextEffectPreset(val label: String) {
    NORMAL("Clean Minimal Flat"),
    GLOW("Electric Cyber Neon Glow"),
    STROKE_3D("3D Heavy Comic Stroke"),
    SHADOW("Soft Hollywood Drop Shadow"),
    DOUBLE_OUTLINE("Double Outline Contrast"),
    BOX_BACKGROUND("Filled Rounded Highlight Box"),
    METALLIC_CHROME("Metallic Chrome Silver"),
    GOLD_METALLIC("Luxury 24K Royal Gold"),
    FIRE_HEAT("Inferno Fire Blaze Glow"),
    ICE_FROST("Arctic Ice Frost Shimmer"),
    RETRO_EXTRUDE("Retro 3D Isometric Extrude"),
    GRADIENT_SUNSET("Sunset Orange & Pink"),
    GRADIENT_CYBER("Cyber Cyan & Magenta"),
    GRADIENT_NEON_GREEN("Matrix Acid Lime Gradient"),
    CYBER_SCANLINE("Scanline Glitch Texture"),
    BUBBLE_POP("Glossy Bubble 3D"),
    HOLOGRAPHIC("Prismatic Holographic"),
    NEWSPAPER_PRINT("Vintage Newspaper Ink"),
    VINTAGE_SEPIA("Sepia Historical Letter"),
    CHALKBOARD("Chalkboard White Dust"),
    BEVEL_EMBOSS("Chiseled Stone Bevel"),
    GLOW_AURA("Pulsing Aura Glow"),
    BLOOD_RED("Vampire Crimson Shadow"),
    PASTEL_CREAM("Pastel Aesthetic Cream Box"),
    CINEMA_BADGE("Black Box Subtitle Bar"),
    RAINBOW_SPECTRUM("Rainbow Spectrum Prism")
}

data class AssPresetStyle(
    val name: String,
    val description: String,
    val fontName: String,
    val fontSize: Int,
    val textColorHex: String,
    val outlineColorHex: String,
    val animationPreset: AnimationPreset,
    val textEffect: TextEffectPreset,
    val boxColorHex: String? = null
) {
    companion object {
        val WHITE_BLACK_STROKE = AssPresetStyle(
            name = "White Fill Black Stroke (Pro)",
            description = "Solid white fill with heavy black outline border for maximum readability",
            fontName = "Anton",
            fontSize = 44,
            textColorHex = "#FFFFFF",
            outlineColorHex = "#000000",
            animationPreset = AnimationPreset.POP_UP,
            textEffect = TextEffectPreset.STROKE_3D
        )

        // --- Viral Shorts & Top Creator Templates ---
        val HORMOZI_BOLD = AssPresetStyle(
            name = "Hormozi Viral Yellow",
            description = "High-retention vibrant yellow with bold black outline and pop-up motion",
            fontName = "Anton",
            fontSize = 44,
            textColorHex = "#FFE600",
            outlineColorHex = "#000000",
            animationPreset = AnimationPreset.POP_UP,
            textEffect = TextEffectPreset.STROKE_3D
        )

        val MR_BEAST_PUNCH = AssPresetStyle(
            name = "MrBeast Cyan Punch",
            description = "Ultra vibrant cyan with punchy bounce and 3D depth",
            fontName = "Impact",
            fontSize = 44,
            textColorHex = "#00F0FF",
            outlineColorHex = "#000000",
            animationPreset = AnimationPreset.BOUNCE,
            textEffect = TextEffectPreset.STROKE_3D
        )

        val TIKTOK_YELLOW_BOX = AssPresetStyle(
            name = "TikTok Yellow Marker Box",
            description = "Black text on vibrant yellow rounded highlight box",
            fontName = "Montserrat",
            fontSize = 36,
            textColorHex = "#000000",
            outlineColorHex = "#FFE600",
            animationPreset = AnimationPreset.POP_UP,
            textEffect = TextEffectPreset.BOX_BACKGROUND,
            boxColorHex = "#FFE600"
        )

        val REELS_LIME_PUNCH = AssPresetStyle(
            name = "Reels Neon Lime",
            description = "High-voltage neon lime with dark contrast outline",
            fontName = "Anton",
            fontSize = 42,
            textColorHex = "#39FF14",
            outlineColorHex = "#000000",
            animationPreset = AnimationPreset.ZOOM_PUNCH,
            textEffect = TextEffectPreset.STROKE_3D
        )

        val ALI_ABDAAL_MINIMAL = AssPresetStyle(
            name = "Ali Abdaal Clean Modern",
            description = "Clean aesthetic modern white with gentle fade and soft drop shadow",
            fontName = "Poppins",
            fontSize = 34,
            textColorHex = "#FFFFFF",
            outlineColorHex = "#1E293B",
            animationPreset = AnimationPreset.FADE_IN,
            textEffect = TextEffectPreset.SHADOW
        )

        val VOX_EXPLAINER = AssPresetStyle(
            name = "Vox Documentary Editorial",
            description = "Editorial clean serif typography with subtitle shadow feel",
            fontName = "Playfair Display",
            fontSize = 36,
            textColorHex = "#FFFFFF",
            outlineColorHex = "#0F172A",
            animationPreset = AnimationPreset.SLIDE_UP,
            textEffect = TextEffectPreset.SHADOW
        )

        val KARAOKE_HIGHLIGHTER = AssPresetStyle(
            name = "Karaoke Active Word",
            description = "Energetic rising highlight with high contrast readability",
            fontName = "Montserrat",
            fontSize = 38,
            textColorHex = "#FFFFFF",
            outlineColorHex = "#6366F1",
            animationPreset = AnimationPreset.KARAOKE_POP,
            textEffect = TextEffectPreset.NORMAL
        )

        val ANDREW_TATE_RED = AssPresetStyle(
            name = "War Room Alpha Red",
            description = "Aggressive crimson red with heavy black stroke and camera shake",
            fontName = "Impact",
            fontSize = 46,
            textColorHex = "#FF1744",
            outlineColorHex = "#000000",
            animationPreset = AnimationPreset.STOMP_SLAM,
            textEffect = TextEffectPreset.STROKE_3D
        )

        // --- Cyberpunk, Tech & Gaming ---
        val NEON_CYBER = AssPresetStyle(
            name = "Cyberpunk 2077 Neon",
            description = "Electric cyberpunk glowing text with magenta edge accents",
            fontName = "Bebas Neue",
            fontSize = 42,
            textColorHex = "#00FFE0",
            outlineColorHex = "#FF007F",
            animationPreset = AnimationPreset.WORD_GLOW,
            textEffect = TextEffectPreset.GLOW
        )

        val CYBER_GLITCH = AssPresetStyle(
            name = "Matrix Terminal Glitch",
            description = "Flickering terminal cyber-green with scanline effect",
            fontName = "Orbitron",
            fontSize = 40,
            textColorHex = "#00FF66",
            outlineColorHex = "#003311",
            animationPreset = AnimationPreset.GLITCH_FLICKER,
            textEffect = TextEffectPreset.CYBER_SCANLINE
        )

        val SYNTHWAVE_PURPLE = AssPresetStyle(
            name = "Synthwave Neon Purple",
            description = "Retro vibrant magenta and sunset yellow gradient",
            fontName = "Righteous",
            fontSize = 40,
            textColorHex = "#FF007F",
            outlineColorHex = "#FFE600",
            animationPreset = AnimationPreset.FLIP_X,
            textEffect = TextEffectPreset.GRADIENT_SUNSET
        )

        val GAMER_RGB_BEAST = AssPresetStyle(
            name = "Esports Gamer RGB",
            description = "High energy electric violet with dual stroke punch",
            fontName = "Chakra Petch",
            fontSize = 42,
            textColorHex = "#A855F7",
            outlineColorHex = "#00F0FF",
            animationPreset = AnimationPreset.RGB_SPLIT_MOTION,
            textEffect = TextEffectPreset.DOUBLE_OUTLINE
        )

        val HACKER_TERMINAL = AssPresetStyle(
            name = "CLI Hacker Typewriter",
            description = "Monospace terminal font with mechanical typewriter reveal",
            fontName = "Press Start 2P",
            fontSize = 28,
            textColorHex = "#39FF14",
            outlineColorHex = "#000000",
            animationPreset = AnimationPreset.TYPEWRITER,
            textEffect = TextEffectPreset.NORMAL
        )

        // --- Luxury, Fashion & Cinema ---
        val METALLIC_GOLD = AssPresetStyle(
            name = "Luxury Royal Gold 24K",
            description = "Gleaming warm metallic champagne and deep bronze edge",
            fontName = "Cinzel",
            fontSize = 38,
            textColorHex = "#FFD700",
            outlineColorHex = "#4A3B18",
            animationPreset = AnimationPreset.EXPAND_TRACKING,
            textEffect = TextEffectPreset.GOLD_METALLIC
        )

        val CHROME_SILVER = AssPresetStyle(
            name = "Liquid Chrome Platinum",
            description = "Futuristic liquid mercury metallic silver with high contrast outline",
            fontName = "Syne",
            fontSize = 40,
            textColorHex = "#E2E8F0",
            outlineColorHex = "#1E293B",
            animationPreset = AnimationPreset.RUBBER_BAND,
            textEffect = TextEffectPreset.METALLIC_CHROME
        )

        val CINEMA_SUBTITLE = AssPresetStyle(
            name = "Hollywood 35mm Master",
            description = "Traditional 24fps movie subtitle format with micro shadow",
            fontName = "Roboto",
            fontSize = 32,
            textColorHex = "#FFF9C4",
            outlineColorHex = "#000000",
            animationPreset = AnimationPreset.FADE_IN,
            textEffect = TextEffectPreset.SHADOW
        )

        val VOGUE_MAGAZINE = AssPresetStyle(
            name = "Vogue High Fashion",
            description = "Elegant high-contrast serif with tracking expansion",
            fontName = "Cormorant",
            fontSize = 36,
            textColorHex = "#FFFFFF",
            outlineColorHex = "#000000",
            animationPreset = AnimationPreset.EXPAND_TRACKING,
            textEffect = TextEffectPreset.SHADOW
        )

        val ROSE_GOLD_LUXE = AssPresetStyle(
            name = "Rose Gold Champagne",
            description = "Soft pink gold luster with warm glowing aura",
            fontName = "Sacramento",
            fontSize = 44,
            textColorHex = "#F43F5E",
            outlineColorHex = "#FFF1F2",
            animationPreset = AnimationPreset.WAVE_FLOAT,
            textEffect = TextEffectPreset.GLOW_AURA
        )

        // --- Elemental, Fire & Ice ---
        val FIRE_BURST = AssPresetStyle(
            name = "Inferno Lava Blaze",
            description = "Burning bright orange with deep flame stroke",
            fontName = "Impact",
            fontSize = 44,
            textColorHex = "#FF4500",
            outlineColorHex = "#FFE600",
            animationPreset = AnimationPreset.SHAKE_IMPACT,
            textEffect = TextEffectPreset.FIRE_HEAT
        )

        val ICE_CHILL = AssPresetStyle(
            name = "Arctic Glacial Frost",
            description = "Crystalline glacial cyan with chilly glow",
            fontName = "Bebas Neue",
            fontSize = 42,
            textColorHex = "#E0F7FA",
            outlineColorHex = "#00B0FF",
            animationPreset = AnimationPreset.BLUR_IN,
            textEffect = TextEffectPreset.ICE_FROST
        )

        val TOXIC_RADIOACTIVE = AssPresetStyle(
            name = "Toxic Biohazard Green",
            description = "Radioactive glowing lime with dark acid rim",
            fontName = "Kanit",
            fontSize = 42,
            textColorHex = "#84CC16",
            outlineColorHex = "#14532D",
            animationPreset = AnimationPreset.HEARTBEAT_PULSE,
            textEffect = TextEffectPreset.GLOW
        )

        val THUNDER_STRIKE = AssPresetStyle(
            name = "Electric Thunder Spark",
            description = "Lightning fast high-voltage white with deep electric blue aura",
            fontName = "Bungee",
            fontSize = 40,
            textColorHex = "#FFFFFF",
            outlineColorHex = "#2563EB",
            animationPreset = AnimationPreset.STROBE_FLASH,
            textEffect = TextEffectPreset.DOUBLE_OUTLINE
        )

        // --- Comic, Cartoon & Anime ---
        val COMIC_BANG = AssPresetStyle(
            name = "Marvel Comic Superhero",
            description = "Punchy graphic comic style with thick white stroke",
            fontName = "Bangers",
            fontSize = 46,
            textColorHex = "#FF1744",
            outlineColorHex = "#FFFFFF",
            animationPreset = AnimationPreset.COMIC_BOOM,
            textEffect = TextEffectPreset.DOUBLE_OUTLINE
        )

        val MANGA_SHONEN = AssPresetStyle(
            name = "Shonen Anime Action",
            description = "Angled dynamic black & white with speed impact motion",
            fontName = "Archivo Black",
            fontSize = 44,
            textColorHex = "#FFFFFF",
            outlineColorHex = "#000000",
            animationPreset = AnimationPreset.SKEW_SNAP,
            textEffect = TextEffectPreset.STROKE_3D
        )

        val CANDY_POP_BUBBLE = AssPresetStyle(
            name = "Sweet Candy Bubble",
            description = "Playful bubblegum pink with white candy outline",
            fontName = "Fredoka",
            fontSize = 42,
            textColorHex = "#EC4899",
            outlineColorHex = "#FFFFFF",
            animationPreset = AnimationPreset.BOBBLE_HEAD,
            textEffect = TextEffectPreset.BUBBLE_POP
        )

        val RETRO_ARCADE_80S = AssPresetStyle(
            name = "80s Arcade Pixel Hook",
            description = "Pixel art retro arcade text with blinking strike",
            fontName = "Press Start 2P",
            fontSize = 26,
            textColorHex = "#F59E0B",
            outlineColorHex = "#7C2D12",
            animationPreset = AnimationPreset.ARCADE_BLINK,
            textEffect = TextEffectPreset.STROKE_3D
        )

        // --- Aesthetic, Lofi & Pastel ---
        val LOFI_DREAM = AssPresetStyle(
            name = "Lofi Pastel Chill",
            description = "Soft warm peach with cozy rounded background pill",
            fontName = "Comfortaa",
            fontSize = 32,
            textColorHex = "#334155",
            outlineColorHex = "#FCE7F3",
            animationPreset = AnimationPreset.FADE_IN,
            textEffect = TextEffectPreset.PASTEL_CREAM,
            boxColorHex = "#FCE7F3"
        )

        val RETRO_VINTAGE_POSTER = AssPresetStyle(
            name = "70s Retro Vintage Warm",
            description = "Burnt orange and avocado green vintage groove",
            fontName = "Righteous",
            fontSize = 38,
            textColorHex = "#EA580C",
            outlineColorHex = "#FEF3C7",
            animationPreset = AnimationPreset.ROLL_IN,
            textEffect = TextEffectPreset.RETRO_EXTRUDE
        )

        val CINEMA_BLACK_BAR = AssPresetStyle(
            name = "Cinematic Solid Black Bar",
            description = "High readability crisp white text on pure black bar",
            fontName = "Inter",
            fontSize = 34,
            textColorHex = "#FFFFFF",
            outlineColorHex = "#000000",
            animationPreset = AnimationPreset.FADE_IN,
            textEffect = TextEffectPreset.CINEMA_BADGE,
            boxColorHex = "#CC000000"
        )

        val CAPCUT_VIRAL_DEFAULT = AssPresetStyle(
            name = "CapCut Viral Signature",
            description = "Signature white bold text with sleek cyan shadow and bounce snap",
            fontName = "Anton",
            fontSize = 44,
            textColorHex = "#FFFFFF",
            outlineColorHex = "#00F0FF",
            animationPreset = AnimationPreset.CAPCUT_FAST_BOUNCE,
            textEffect = TextEffectPreset.STROKE_3D
        )

        val ALL_PRESETS = listOf(
            WHITE_BLACK_STROKE,
            HORMOZI_BOLD,
            MR_BEAST_PUNCH,
            CAPCUT_VIRAL_DEFAULT,
            TIKTOK_YELLOW_BOX,
            REELS_LIME_PUNCH,
            KARAOKE_HIGHLIGHTER,
            ALI_ABDAAL_MINIMAL,
            VOX_EXPLAINER,
            ANDREW_TATE_RED,
            NEON_CYBER,
            CYBER_GLITCH,
            SYNTHWAVE_PURPLE,
            GAMER_RGB_BEAST,
            HACKER_TERMINAL,
            METALLIC_GOLD,
            CHROME_SILVER,
            CINEMA_SUBTITLE,
            CINEMA_BLACK_BAR,
            VOGUE_MAGAZINE,
            ROSE_GOLD_LUXE,
            FIRE_BURST,
            ICE_CHILL,
            TOXIC_RADIOACTIVE,
            THUNDER_STRIKE,
            COMIC_BANG,
            MANGA_SHONEN,
            CANDY_POP_BUBBLE,
            RETRO_ARCADE_80S,
            LOFI_DREAM,
            RETRO_VINTAGE_POSTER
        )
    }
}

object TypographyList {
    val ALL_FONTS = listOf(
        "Anton", "Impact", "Bebas Neue", "Montserrat", "Poppins", "Oswald", "Roboto", "Inter",
        "Playfair Display", "Cinzel", "Bangers", "Bungee", "Righteous", "Orbitron", "Cabin Sketch",
        "Permanent Marker", "Special Elite", "Archivo Black", "Kanit", "Syne", "Pacifico", "Lobster",
        "Dancing Script", "Russo One", "Satisfy", "Sacramento", "Cormorant", "Abril Fatface",
        "Comfortaa", "Caveat", "Josefin Sans", "Fredoka", "Chakra Petch", "Press Start 2P", "Creepster",
        "Merriweather", "Raleway", "Nunito", "Rubik", "Titillium Web", "DM Sans", "Teko",
        "Fjalla One", "Alfa Slab One", "Carter One", "Shrikhand", "Courgette", "Ultra", "Monoton"
    )
}

object ColorPalette {
    val SWATCHES = listOf(
        // High Impact Yellows & Golds (Hormozi, MrBeast)
        "#FFE600", "#FFD700", "#FACC15", "#F59E0B", "#D97706", "#B45309",
        // Viral Cyans & Blues
        "#00F0FF", "#06B6D4", "#22D3EE", "#38BDF8", "#3B82F6", "#6366F1", "#1D4ED8", "#0284C7",
        // Neons & Greens
        "#39FF14", "#00FF66", "#10B981", "#22C55E", "#84CC16", "#A3E635", "#16A34A",
        // Hot Pinks, Reds & Magentas
        "#FF007F", "#EC4899", "#F43F5E", "#EF4444", "#DC2626", "#B91C1C", "#FF1744", "#E11D48",
        // Purples & Violets
        "#A855F7", "#8B5CF6", "#7C3AED", "#6D28D9", "#C084FC", "#9333EA",
        // Oranges & Corals
        "#FF6B00", "#FF4500", "#F97316", "#FB923C", "#EA580C", "#C2410C",
        // Whites, Creams & Monochrome
        "#FFFFFF", "#F8FAFC", "#F1F5F9", "#E2E8F0", "#CBD5E1", "#94A3B8", "#64748B", "#475569", "#1E293B", "#0F172A", "#000000"
    )
}

data class TextSyncItem(
    val id: String,
    val text: String,
    val startSeconds: Double,
    val endSeconds: Double,
    val animationPreset: AnimationPreset = AnimationPreset.POP_UP,
    val textEffect: TextEffectPreset = TextEffectPreset.STROKE_3D,
    val fontName: String = "Anton",
    val fontSize: Int = 40,
    val textColorHex: String = "#FFFFFF",
    val outlineColorHex: String = "#000000",
    val boxColorHex: String? = null,
    val posX: Float = 0.5f, // 0.0 (left) to 1.0 (right), 0.5 is center
    val posY: Float = 0.78f // 0.0 (top) to 1.0 (bottom), 0.78 is ideal lower-third
)
