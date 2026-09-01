package com.oroclub.game.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oroclub.game.data.GameRepository
import com.oroclub.game.domain.GameLogic
import com.oroclub.game.domain.QuizContent
import com.oroclub.game.ui.components.FallbackScreen
import com.oroclub.game.ui.screens.LevelsScreen
import com.oroclub.game.ui.screens.MainScreen
import com.oroclub.game.ui.screens.MenuScreen
import com.oroclub.game.ui.screens.PairsResultsScreen
import com.oroclub.game.ui.screens.PairsScreen
import com.oroclub.game.ui.screens.QuizCategoryScreen
import com.oroclub.game.ui.screens.QuizResultsScreen
import com.oroclub.game.ui.screens.QuizScreen
import com.oroclub.game.ui.screens.ResultsSummaryScreen
import com.oroclub.game.ui.screens.RulesScreen
import com.oroclub.game.ui.screens.SettingsScreen
import com.oroclub.game.ui.vm.PairsStatus
import com.oroclub.game.ui.vm.PairsViewModel
import com.oroclub.game.ui.vm.ProgressViewModel
import com.oroclub.game.ui.vm.QuizViewModel

/**
 * The whole navigation graph.
 *
 * Every destination validates its arguments before using them. A missing or
 * invalid argument, an emptied store or a process recreation lands on a
 * friendly [FallbackScreen] with a working Back instead of crashing.
 */
@Composable
fun OroNavGraph(
    repository: GameRepository,
    navController: NavHostController = rememberNavController()
) {
    val progressViewModel: ProgressViewModel =
        viewModel(factory = ProgressViewModel.Factory(repository))
    val progress by progressViewModel.uiState.collectAsStateWithLifecycle()

    /** Pops back to a known-good destination, falling back to the root. */
    fun backToMenu() {
        if (!navController.popBackStack(Routes.MENU, inclusive = false)) {
            navController.navigate(Routes.MENU) {
                popUpTo(Routes.MAIN) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.MAIN
    ) {

        composable(Routes.MAIN) {
            MainScreen(
                state = progress,
                onStart = { navController.navigate(Routes.MENU) { launchSingleTop = true } }
            )
        }

        composable(Routes.MENU) {
            MenuScreen(
                onQuiz = { navController.navigate(Routes.QUIZ_CATEGORIES) { launchSingleTop = true } },
                onPairs = { navController.navigate(Routes.LEVELS) { launchSingleTop = true } },
                onResults = { navController.navigate(Routes.RESULTS) { launchSingleTop = true } },
                onSettings = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                onRules = { navController.navigate(Routes.RULES) { launchSingleTop = true } },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.QUIZ_CATEGORIES) {
            QuizCategoryScreen(
                state = progress,
                onCategory = { id -> navController.navigate(Routes.quiz(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.QUIZ,
            arguments = listOf(navArgument(Routes.ARG_CATEGORY_ID) {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { entry ->
            val categoryId = entry.arguments?.getString(Routes.ARG_CATEGORY_ID).orEmpty()
            val category = QuizContent.categoryById(categoryId)
            if (category == null) {
                FallbackScreen(
                    message = "That quiz deck is no longer available.",
                    onBack = { navController.popBackStack() }
                )
            } else {
                val quizViewModel: QuizViewModel = viewModel(
                    factory = QuizViewModel.Factory(repository, category.id)
                )
                val state by quizViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.finished) {
                    if (state.finished) {
                        navController.navigate(Routes.quizResults(category.id, state.score)) {
                            popUpTo(Routes.QUIZ) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                QuizScreen(
                    state = state,
                    onSelect = quizViewModel::selectAnswer,
                    onPause = quizViewModel::pause,
                    onResume = quizViewModel::resume,
                    onRestart = {
                        quizViewModel.restart()
                        quizViewModel.resume()
                    },
                    onMenu = { backToMenu() },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Routes.QUIZ_RESULTS,
            arguments = listOf(
                navArgument(Routes.ARG_CATEGORY_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(Routes.ARG_SCORE) {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { entry ->
            val categoryId = entry.arguments?.getString(Routes.ARG_CATEGORY_ID).orEmpty()
            val category = QuizContent.categoryById(categoryId)
            val score = (entry.arguments?.getInt(Routes.ARG_SCORE) ?: 0)
                .coerceIn(0, QuizContent.QUESTIONS_PER_CATEGORY)
            if (category == null) {
                FallbackScreen(
                    message = "These results belong to a deck that is no longer available.",
                    onBack = { backToMenu() }
                )
            } else {
                QuizResultsScreen(
                    categoryTitle = category.title,
                    score = score,
                    total = QuizContent.QUESTIONS_PER_CATEGORY,
                    onContinue = {
                        if (!navController.popBackStack(Routes.QUIZ_CATEGORIES, inclusive = false)) {
                            backToMenu()
                        }
                    },
                    onRestart = {
                        navController.navigate(Routes.quiz(category.id)) {
                            popUpTo(Routes.QUIZ_CATEGORIES) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onMenu = { backToMenu() }
                )
            }
        }

        composable(Routes.LEVELS) {
            LevelsScreen(
                state = progress,
                onLevel = { level -> navController.navigate(Routes.pairs(level)) },
                onRules = { navController.navigate(Routes.RULES) { launchSingleTop = true } },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PAIRS,
            arguments = listOf(navArgument(Routes.ARG_LEVEL) {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { entry ->
            val level = entry.arguments?.getInt(Routes.ARG_LEVEL) ?: 0
            if (!GameLogic.isValidLevel(level)) {
                FallbackScreen(
                    message = "That level does not exist in this build.",
                    onBack = { navController.popBackStack() }
                )
            } else {
                val pairsViewModel: PairsViewModel = viewModel(
                    factory = PairsViewModel.Factory(repository, level)
                )
                val state by pairsViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.status) {
                    if (state.status != PairsStatus.PLAYING) {
                        navController.navigate(
                            Routes.pairsResults(
                                level = state.level,
                                matched = state.matchedPairs,
                                total = state.totalPairs,
                                won = state.status == PairsStatus.WON
                            )
                        ) {
                            popUpTo(Routes.PAIRS) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                PairsScreen(
                    state = state,
                    onFlip = pairsViewModel::flip,
                    onPause = pairsViewModel::pause,
                    onLeaveForeground = pairsViewModel::pauseForBackground,
                    onResume = pairsViewModel::resume,
                    onRestart = {
                        pairsViewModel.restart()
                        pairsViewModel.resume()
                    },
                    onMenu = { backToMenu() },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Routes.PAIRS_RESULTS,
            arguments = listOf(
                navArgument(Routes.ARG_LEVEL) { type = NavType.IntType; defaultValue = 0 },
                navArgument(Routes.ARG_MATCHED) { type = NavType.IntType; defaultValue = 0 },
                navArgument(Routes.ARG_TOTAL) { type = NavType.IntType; defaultValue = 0 },
                navArgument(Routes.ARG_WON) { type = NavType.IntType; defaultValue = 0 }
            )
        ) { entry ->
            val level = entry.arguments?.getInt(Routes.ARG_LEVEL) ?: 0
            val config = GameLogic.levelConfig(level)
            if (config == null) {
                FallbackScreen(
                    message = "These results belong to a level that no longer exists.",
                    onBack = { backToMenu() }
                )
            } else {
                val total = (entry.arguments?.getInt(Routes.ARG_TOTAL) ?: config.pairs)
                    .takeIf { it > 0 } ?: config.pairs
                val matched = (entry.arguments?.getInt(Routes.ARG_MATCHED) ?: 0).coerceIn(0, total)
                val won = (entry.arguments?.getInt(Routes.ARG_WON) ?: 0) == 1
                val nextLevel = level + 1
                val hasNext = GameLogic.isValidLevel(nextLevel)

                PairsResultsScreen(
                    level = level,
                    matched = matched,
                    total = total,
                    won = won,
                    hasNextLevel = hasNext,
                    onContinue = {
                        if (won && hasNext) {
                            navController.navigate(Routes.pairs(nextLevel)) {
                                popUpTo(Routes.LEVELS) { inclusive = false }
                                launchSingleTop = true
                            }
                        } else if (!navController.popBackStack(Routes.LEVELS, inclusive = false)) {
                            backToMenu()
                        }
                    },
                    onRestart = {
                        navController.navigate(Routes.pairs(level)) {
                            popUpTo(Routes.LEVELS) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onMenu = { backToMenu() }
                )
            }
        }

        composable(Routes.RESULTS) {
            ResultsSummaryScreen(
                state = progress,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                soundEnabled = progress.soundEnabled,
                onSoundChange = progressViewModel::setSoundEnabled,
                onReset = progressViewModel::resetProgress,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.RULES) {
            RulesScreen(onBack = { navController.popBackStack() })
        }
    }
}
