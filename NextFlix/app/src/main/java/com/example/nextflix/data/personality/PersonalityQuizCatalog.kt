package com.example.nextflix.data.personality

object PersonalityQuizCatalog {
    val questions: List<PersonalityQuizQuestion> = listOf(
        PersonalityQuizQuestion(
            id = "free_time",
            dimension = PersonalityDimension.LEISURE_STYLE,
            prompt = "How do you usually spend your free time?",
            options = listOf(
                PersonalityQuizOption("ft_social", "With friends or family"),
                PersonalityQuizOption("ft_creative", "Making or learning something creative"),
                PersonalityQuizOption("ft_calm", "Quiet time alone to recharge"),
                PersonalityQuizOption("ft_active", "Being out and doing activities")
            )
        ),
        PersonalityQuizQuestion(
            id = "structure",
            dimension = PersonalityDimension.STRUCTURE,
            prompt = "Do you prefer structure or spontaneity?",
            options = listOf(
                PersonalityQuizOption("str_planned", "Clear plans and routines"),
                PersonalityQuizOption("str_flexible", "A loose plan I can change"),
                PersonalityQuizOption("str_spontaneous", "Going with the moment"),
                PersonalityQuizOption("str_mix", "A mix depending on my mood")
            )
        ),
        PersonalityQuizQuestion(
            id = "pace",
            dimension = PersonalityDimension.PACING,
            prompt = "What kind of pace do you enjoy most?",
            options = listOf(
                PersonalityQuizOption("pace_slow", "Slow and reflective"),
                PersonalityQuizOption("pace_steady", "Steady with room to breathe"),
                PersonalityQuizOption("pace_brisk", "Brisk and engaging"),
                PersonalityQuizOption("pace_intense", "Fast and high-energy")
            )
        ),
        PersonalityQuizQuestion(
            id = "story_tone",
            dimension = PersonalityDimension.STORY_TONE,
            prompt = "Do you lean toward emotional stories or more exciting, adventurous ones?",
            options = listOf(
                PersonalityQuizOption("tone_emotional", "Deep, emotional stories"),
                PersonalityQuizOption("tone_adventure", "Excitement and adventure"),
                PersonalityQuizOption("tone_balanced", "A balance of both"),
                PersonalityQuizOption("tone_surprise", "Whatever feels fresh and surprising")
            )
        ),
        PersonalityQuizQuestion(
            id = "setting",
            dimension = PersonalityDimension.SETTING,
            prompt = "Do you prefer realistic settings or imaginative worlds?",
            options = listOf(
                PersonalityQuizOption("set_real", "Grounded, realistic settings"),
                PersonalityQuizOption("set_imaginative", "Imaginative or fantastical worlds"),
                PersonalityQuizOption("set_historical", "Historical or period settings"),
                PersonalityQuizOption("set_any", "No strong preference")
            )
        ),
        PersonalityQuizQuestion(
            id = "comfort_thought",
            dimension = PersonalityDimension.EMOTIONAL_GOAL,
            prompt = "Do you usually want something comforting or something thought-provoking?",
            options = listOf(
                PersonalityQuizOption("goal_comfort", "Comforting and uplifting"),
                PersonalityQuizOption("goal_thought", "Thought-provoking and challenging"),
                PersonalityQuizOption("goal_both", "Depends on the day"),
                PersonalityQuizOption("goal_escape", "Pure escapism")
            )
        ),
        PersonalityQuizQuestion(
            id = "social_context",
            dimension = PersonalityDimension.SOCIAL_CONTEXT,
            prompt = "When you pick a movie or book, do you imagine enjoying it mostly…",
            options = listOf(
                PersonalityQuizOption("soc_solo", "On your own"),
                PersonalityQuizOption("soc_partner", "With one other person"),
                PersonalityQuizOption("soc_group", "With a group"),
                PersonalityQuizOption("soc_either", "Either way works")
            )
        ),
        PersonalityQuizQuestion(
            id = "commitment",
            dimension = PersonalityDimension.COMMITMENT,
            prompt = "How do you feel about longer stories (series, long novels, epics)?",
            options = listOf(
                PersonalityQuizOption("com_short", "Prefer shorter, complete experiences"),
                PersonalityQuizOption("com_medium", "Medium length is ideal"),
                PersonalityQuizOption("com_long", "Love sinking into long stories"),
                PersonalityQuizOption("com_varies", "Varies by mood")
            )
        )
    )
}
