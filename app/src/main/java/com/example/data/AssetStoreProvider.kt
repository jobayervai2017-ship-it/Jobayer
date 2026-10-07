package com.example.data

import kotlinx.coroutines.delay

data class AssetItem(
    val id: String,
    val name: String,
    val category: AssetCategory,
    val subCategory: String,
    val previewHex: String = "#7C3AED",
    var isDownloaded: Boolean = true,
    val effectTag: String = ""
)

enum class AssetCategory(val label: String) {
    ANIMATION("Animations"),
    EFFECT("Effects"),
    FILTER("Filters"),
    TRANSITION("Transitions")
}

object AssetStoreProvider {

    val CATALOG: List<AssetItem> by lazy {
        listOf(
            // --- ANIMATIONS: IN ---
            AssetItem("anim_in_pop", "Hormozi Pop-Up", AssetCategory.ANIMATION, "In", "#FFE600", true, "POP_UP"),
            AssetItem("anim_in_bounce", "Punchy Bounce", AssetCategory.ANIMATION, "In", "#00F0FF", true, "BOUNCE"),
            AssetItem("anim_in_zoom", "Crash Zoom Punch", AssetCategory.ANIMATION, "In", "#FF007F", true, "ZOOM_PUNCH"),
            AssetItem("anim_in_elastic", "Elastic Spring Drop", AssetCategory.ANIMATION, "In", "#10B981", false, "ELASTIC_DROP"),
            AssetItem("anim_in_slam", "Heavy Stomp Slam", AssetCategory.ANIMATION, "In", "#EF4444", true, "STOMP_SLAM"),
            AssetItem("anim_in_snap", "Instant Snap In", AssetCategory.ANIMATION, "In", "#F59E0B", true, "SNAP_IN"),
            AssetItem("anim_in_slide_up", "Slide Up Smooth", AssetCategory.ANIMATION, "In", "#3B82F6", true, "SLIDE_UP"),
            AssetItem("anim_in_slide_left", "Slide Left Track", AssetCategory.ANIMATION, "In", "#8B5CF6", false, "SLIDE_LEFT"),
            AssetItem("anim_in_flip_x", "3D Flip Horizontal", AssetCategory.ANIMATION, "In", "#EC4899", false, "FLIP_X"),
            AssetItem("anim_in_typewriter", "Terminal Code Type", AssetCategory.ANIMATION, "In", "#14B8A6", true, "TYPEWRITER"),
            AssetItem("anim_in_drift", "Cinematic Slow Drift", AssetCategory.ANIMATION, "In", "#6366F1", true, "DRIFT_SLOW"),
            AssetItem("anim_in_glitch_pop", "Cyber Glitch Pop", AssetCategory.ANIMATION, "In", "#D946EF", false, "GLITCH_FLICKER"),

            // --- ANIMATIONS: OUT ---
            AssetItem("anim_out_fade", "Smooth Dissolve Out", AssetCategory.ANIMATION, "Out", "#64748B", true, "FADE_IN"),
            AssetItem("anim_out_slide_down", "Slide Down Exit", AssetCategory.ANIMATION, "Out", "#475569", true, "SLIDE_DOWN"),
            AssetItem("anim_out_zoom_out", "Cinematic Zoom Pull", AssetCategory.ANIMATION, "Out", "#334155", false, "ZOOM_OUT"),
            AssetItem("anim_out_smoke", "Smoke Cloud Dissolve", AssetCategory.ANIMATION, "Out", "#1E293B", false, "SMOKE_DISSOLVE"),
            AssetItem("anim_out_shrink", "Pinch Shrink Drop", AssetCategory.ANIMATION, "Out", "#0F172A", true, "SCALE_DOWN"),
            AssetItem("anim_out_wipe_right", "Linear Wipe Right", AssetCategory.ANIMATION, "Out", "#64748B", true, "SLIDE_RIGHT"),
            AssetItem("anim_out_flip_exit", "3D Flip Spin Out", AssetCategory.ANIMATION, "Out", "#7C3AED", false, "ROLL_IN"),

            // --- ANIMATIONS: COMBO ---
            AssetItem("anim_combo_wiggle", "TikTok Dynamic Wiggle", AssetCategory.ANIMATION, "Combo", "#F43F5E", true, "TIKTOK_WIGGLE"),
            AssetItem("anim_combo_pulse", "Heartbeat Pulse", AssetCategory.ANIMATION, "Combo", "#E11D48", true, "HEARTBEAT_PULSE"),
            AssetItem("anim_combo_roll", "Kinetic Barrel Roll", AssetCategory.ANIMATION, "Combo", "#D946EF", false, "ROLL_IN"),
            AssetItem("anim_combo_swing", "Pendulum Clock Swing", AssetCategory.ANIMATION, "Combo", "#A855F7", true, "SWING_PENDULUM"),
            AssetItem("anim_combo_glitch", "Cyber Glitch Flicker", AssetCategory.ANIMATION, "Combo", "#06B6D4", false, "GLITCH_FLICKER"),
            AssetItem("anim_combo_capcut", "CapCut Fast Bounce", AssetCategory.ANIMATION, "Combo", "#6366F1", true, "CAPCUT_FAST_BOUNCE"),
            AssetItem("anim_combo_float", "Zero Gravity Float", AssetCategory.ANIMATION, "Combo", "#10B981", true, "FLOAT_SLOW"),
            AssetItem("anim_combo_quake", "Earthquake Rumble", AssetCategory.ANIMATION, "Combo", "#EF4444", false, "EARTHQUAKE"),

            // --- EFFECTS: VIDEO EFFECTS ---
            AssetItem("fx_neon_glow", "Cyber Neon Glow", AssetCategory.EFFECT, "Video Effects", "#00F0FF", true, "GLOW"),
            AssetItem("fx_3d_stroke", "3D Heavy Comic Stroke", AssetCategory.EFFECT, "Video Effects", "#FFE600", true, "STROKE_3D"),
            AssetItem("fx_gold", "24K Royal Gold", AssetCategory.EFFECT, "Video Effects", "#F59E0B", false, "GOLD_METALLIC"),
            AssetItem("fx_chrome", "Metallic Chrome Silver", AssetCategory.EFFECT, "Video Effects", "#94A3B8", true, "METALLIC_CHROME"),
            AssetItem("fx_fire", "Inferno Fire Blaze", AssetCategory.EFFECT, "Video Effects", "#EA580C", false, "FIRE_HEAT"),
            AssetItem("fx_ice", "Arctic Ice Frost", AssetCategory.EFFECT, "Video Effects", "#38BDF8", true, "ICE_FROST"),
            AssetItem("fx_shadow", "Soft Hollywood Shadow", AssetCategory.EFFECT, "Video Effects", "#0F172A", true, "SHADOW"),
            AssetItem("fx_double_out", "Double Outline Contrast", AssetCategory.EFFECT, "Video Effects", "#EC4899", false, "DOUBLE_OUTLINE"),
            AssetItem("fx_rgb_split", "Chromatic RGB Split", AssetCategory.EFFECT, "Video Effects", "#06B6D4", false, "CHROMATIC_ABERRATION"),
            AssetItem("fx_halftone", "Vintage Comic Halftone", AssetCategory.EFFECT, "Video Effects", "#FACC15", true, "HALFTONE_PRINT"),

            // --- EFFECTS: BODY & ATMOSPHERE ---
            AssetItem("fx_hologram", "Prismatic Holographic", AssetCategory.EFFECT, "Atmosphere", "#818CF8", false, "HOLOGRAPHIC"),
            AssetItem("fx_bubble", "Glossy Bubble 3D", AssetCategory.EFFECT, "Atmosphere", "#C084FC", true, "BUBBLE_POP"),
            AssetItem("fx_scanline", "CRT Scanline Texture", AssetCategory.EFFECT, "Atmosphere", "#2DD4BF", false, "CYBER_SCANLINE"),
            AssetItem("fx_rainbow", "Rainbow Spectrum Prism", AssetCategory.EFFECT, "Atmosphere", "#F472B6", true, "RAINBOW_SPECTRUM"),
            AssetItem("fx_dust_particles", "Floating Gold Dust", AssetCategory.EFFECT, "Atmosphere", "#F59E0B", false, "LIGHT_LEAK"),
            AssetItem("fx_anamorphic", "Anamorphic Blue Flare", AssetCategory.EFFECT, "Atmosphere", "#3B82F6", true, "ANAMORPHIC_BLUE"),

            // --- FILTERS: CINEMA ---
            AssetItem("filt_teal_orange", "Hollywood Teal & Orange", AssetCategory.FILTER, "Cinema", "#0284C7", true, "TEAL_ORANGE"),
            AssetItem("filt_warm", "Cinematic Golden Warm", AssetCategory.FILTER, "Cinema", "#D97706", true, "CINEMATIC_WARM"),
            AssetItem("filt_cold_steel", "Cold Steel Thriller", AssetCategory.FILTER, "Cinema", "#475569", true, "COLD_STEEL"),
            AssetItem("filt_bleach", "Bleach Bypass Action", AssetCategory.FILTER, "Cinema", "#64748B", false, "BLEACH_BYPASS"),
            AssetItem("filt_golden", "Magic Golden Hour", AssetCategory.FILTER, "Cinema", "#B45309", true, "GOLDEN_HOUR"),
            AssetItem("filt_desert", "Sahara Desert Heat", AssetCategory.FILTER, "Cinema", "#C2410C", false, "DESERT_WARMTH"),
            AssetItem("filt_kodak", "Kodachrome 64 Classic", AssetCategory.FILTER, "Cinema", "#E11D48", true, "KODACHROME"),
            AssetItem("filt_moody", "Moody Dark Emerald", AssetCategory.FILTER, "Cinema", "#065F46", false, "DARK_MOODY"),

            // --- FILTERS: RETRO & VINTAGE ---
            AssetItem("filt_vhs_84", "Vintage VHS 1984", AssetCategory.FILTER, "Retro", "#9333EA", true, "VINTAGE_VHS"),
            AssetItem("filt_90s_cam", "Retro 90s Camcorder", AssetCategory.FILTER, "Retro", "#7E22CE", true, "RETRO_90S"),
            AssetItem("filt_film_35", "35mm Celluloid Grain", AssetCategory.FILTER, "Retro", "#581C87", false, "FILM_GRAIN"),
            AssetItem("filt_sepia", "Antique Sepia 1890", AssetCategory.FILTER, "Retro", "#78350F", true, "SEPIA_CLASSIC"),
            AssetItem("filt_polaroid", "Polaroid Instant 600", AssetCategory.FILTER, "Retro", "#A16207", false, "POLAROID_600"),
            AssetItem("filt_technicolor", "1930s Technicolor", AssetCategory.FILTER, "Retro", "#BE123C", true, "TECHNICOLOR_VINTAGE"),

            // --- FILTERS: CYBER & MOODY ---
            AssetItem("filt_cyberpunk", "Cyberpunk Neon 2077", AssetCategory.FILTER, "Cyber", "#06B6D4", true, "CYBERPUNK"),
            AssetItem("filt_matrix", "Matrix Terminal Green", AssetCategory.FILTER, "Cyber", "#16A34A", true, "MATRIX_GREEN"),
            AssetItem("filt_synthwave", "Synthwave Laser Purple", AssetCategory.FILTER, "Cyber", "#C026D3", false, "NEON_PURPLE"),
            AssetItem("filt_noir", "Deep Noir High Contrast", AssetCategory.FILTER, "Cyber", "#18181B", true, "BLACK_WHITE"),
            AssetItem("filt_hdr", "HDR Vivid Ultra Pop", AssetCategory.FILTER, "Cyber", "#DB2777", true, "HDR_VIVID"),
            AssetItem("filt_acid", "Acid Green Neon Pop", AssetCategory.FILTER, "Cyber", "#84CC16", false, "ACID_GREEN"),

            // --- TRANSITIONS: CAMERA & MOTION ---
            AssetItem("trans_crossfade", "Smooth Crossfade", AssetCategory.TRANSITION, "Camera", "#4F46E5", true, "CROSSFADE"),
            AssetItem("trans_zoom_in", "Crash Zoom Punch", AssetCategory.TRANSITION, "Camera", "#7C3AED", true, "ZOOM_IN"),
            AssetItem("trans_whip_left", "Whip Pan Left", AssetCategory.TRANSITION, "Camera", "#2563EB", true, "WHIP_LEFT"),
            AssetItem("trans_spin_cw", "Spin 360 Clockwise", AssetCategory.TRANSITION, "Camera", "#9333EA", false, "SPIN_CLOCKWISE"),
            AssetItem("trans_slide_up", "Slide Up Push", AssetCategory.TRANSITION, "Camera", "#0D9488", true, "SLIDE_UP"),
            AssetItem("trans_roll_right", "Barrel Roll 180", AssetCategory.TRANSITION, "Camera", "#4338CA", true, "ROLL_RIGHT"),

            // --- TRANSITIONS: GLITCH & LIGHT ---
            AssetItem("trans_glitch", "Digital RGB Glitch", AssetCategory.TRANSITION, "Glitch", "#EC4899", true, "GLITCH_BURST"),
            AssetItem("trans_flash", "Strobe White Flash", AssetCategory.TRANSITION, "Glitch", "#FACC15", true, "FLASH_WHITE"),
            AssetItem("trans_film_burn", "Golden Film Burn", AssetCategory.TRANSITION, "Glitch", "#F97316", false, "FILM_BURN"),
            AssetItem("trans_capcut", "CapCut Smooth Zoom", AssetCategory.TRANSITION, "Glitch", "#6366F1", true, "CAPCUT_SMOOTH_ZOOM"),
            AssetItem("trans_light_leak", "Prism Light Leak", AssetCategory.TRANSITION, "Glitch", "#FBBF24", true, "LIGHT_LEAK_WARM")
        )
    }

    suspend fun simulateDownload(item: AssetItem) {
        delay(600) // realistic mock network delay
        item.isDownloaded = true
    }
}
