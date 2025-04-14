package com.dp.logcatapp.ui.screens

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import androidx.activity.compose.BackHandler
import androidx.annotation.WorkerThread
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction.Press
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dp.logcat.Filter
import com.dp.logcat.Log
import com.dp.logcat.LogcatSession
import com.dp.logcat.LogcatSession.RecordingFileInfo
import com.dp.logcat.LogcatUtil
import com.dp.logcatapp.LogcatApp
import com.dp.logcatapp.R
import com.dp.logcatapp.activities.FiltersActivity
import com.dp.logcatapp.activities.SavedLogsActivity
import com.dp.logcatapp.activities.SavedLogsViewerActivity
import com.dp.logcatapp.activities.SettingsActivity
import com.dp.logcatapp.db.FilterInfo
import com.dp.logcatapp.db.LogcatReaderDatabase
import com.dp.logcatapp.db.RegexFilterType
import com.dp.logcatapp.db.SavedLogInfo
import com.dp.logcatapp.db.regexFilterTypes
import com.dp.logcatapp.services.LogcatService
import com.dp.logcatapp.services.LogcatService.LogcatSessionStatus
import com.dp.logcatapp.services.getService
import com.dp.logcatapp.ui.common.CopyLogClipboardBottomSheet
import com.dp.logcatapp.ui.common.HitIndex
import com.dp.logcatapp.ui.common.LOGCAT_DIR
import com.dp.logcatapp.ui.common.LogsList
import com.dp.logcatapp.ui.common.LogsListStyle
import com.dp.logcatapp.ui.common.MaybeShowPermissionRequiredDialog
import com.dp.logcatapp.ui.common.SearchHitKey
import com.dp.logcatapp.ui.common.SearchLogsTopBar
import com.dp.logcatapp.ui.common.SearchResult
import com.dp.logcatapp.ui.common.SearchResult.SearchHit
import com.dp.logcatapp.ui.common.ToggleableLogItem
import com.dp.logcatapp.ui.common.WithTooltip
import com.dp.logcatapp.ui.common.searchLogs
import com.dp.logcatapp.ui.theme.AppTypography
import com.dp.logcatapp.util.AppInfo
import com.dp.logcatapp.util.SettingsPrefKeys
import com.dp.logcatapp.util.ShareUtils
import com.dp.logcatapp.util.getDefaultSharedPreferences
import com.dp.logcatapp.util.rememberAppInfoByUidMap
import com.dp.logcatapp.util.rememberBooleanSharedPreference
import com.dp.logcatapp.util.rememberStringSetSharedPreference
import com.dp.logcatapp.util.showToast
import com.dp.logcatapp.util.toRegexOrNull
import com.dp.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "HomeScreen"
private const val SNAP_SCROLL_HIDE_DELAY_MS = 2000L
private const val COMPACT_VIEW_KEY = "device_logs_compact_view_key"
private const val ENABLED_LOG_ITEMS_KEY = "toggleable_log_items_pref_key"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceLogsScreen(
  modifier: Modifier,
  stopRecordingSignal: Flow<Unit>,
  viewModel: DeviceLogsViewModel = viewModel(
    key = "device_logs_view_model",
  ),
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current

  val appInfoMap by rememberUpdatedState(rememberAppInfoByUidMap())
  val lazyListState = rememberLazyListState()

  val updatedStopRecordingSignal by rememberUpdatedState(stopRecordingSignal)
  var snapToBottom by rememberSaveable { mutableStateOf(true) }

  val scrollToTopInteractionSource = remember { MutableInteractionSource() }
  val scrollToBottomInteractionSource = remember { MutableInteractionSource() }
  val snapScrollInfo = rememberSnapScrollInfo(
    lazyListState = lazyListState,
    snapToBottom = snapToBottom,
    snapUpInteractionSource = scrollToTopInteractionSource,
    snapDownInteractionSource = scrollToBottomInteractionSource,
  )

  var compactViewPreference = rememberBooleanSharedPreference(
    key = COMPACT_VIEW_KEY,
    default = false,
  )
  var toggleableLogItemsPref = rememberStringSetSharedPreference(
    key = ENABLED_LOG_ITEMS_KEY,
    default = ToggleableLogItem.entries.map { it.ordinal.toString() }.toSet(),
  )

  val logsState = remember { mutableStateListOf<Log>() }
  var logcatPaused by remember { mutableStateOf(false) }
  var showDropDownMenu by remember { mutableStateOf(false) }

  var showSearchBar by rememberSaveable { mutableStateOf(false) }
  var searchInProgress by rememberSaveable { mutableStateOf(false) }
  var useRegexForSearch by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var searchRegexError by remember { mutableStateOf(false) }

  // Value: tagIndex start and end.
  val searchHitIndexMap = remember { mutableStateMapOf<SearchHitKey, List<HitIndex>>() }
  var searchHits by remember { mutableStateOf<List<SearchHit>>(emptyList()) }
  var currentSearchHitIndex by remember { mutableIntStateOf(-1) }
  // var currentSearchHitLogId by remember { mutableIntStateOf(-1) }
  var showHitCount by remember { mutableStateOf(false) }
  var recordStatus by viewModel.recordStatus
  val snackbarHostState = remember { SnackbarHostState() }
  var savedLogsSheetState by remember {
    mutableStateOf<SavedLogsBottomSheetState>(SavedLogsBottomSheetState.Hide)
  }
  var appliedFilters by remember { mutableStateOf(false) }
  var isLogcatSessionLoading by remember { mutableStateOf(true) }
  var errorStartingLogcat by remember { mutableStateOf(false) }
  var showDisplayOptions by remember { mutableStateOf(false) }

  if (showSearchBar) {
    BackHandler { showSearchBar = false }
  }

  val logcatService = viewModel.logcatService
  if (logcatService != null) {
    val db = remember(context) { LogcatReaderDatabase.getInstance(context) }
    LaunchedEffect(logcatService) {
      logcatService.logcatSessionStatus
        .filterNotNull()
        .collectLatest { status ->
          if (status is LogcatSessionStatus.Started) {
            coroutineScope {
              val logcatSession = status.session
              logcatPaused = logcatSession.isPaused

              // Listen for changes to `logcatPaused` and pause/resume LogcatSession accordingly.
              launch {
                snapshotFlow { logcatPaused }
                  .collect {
                    logcatSession.isPaused = it
                  }
              }

              if (logcatSession.isRecording) {
                recordStatus = RecordStatus.RecordingInProgress
              }

              launch {
                updatedStopRecordingSignal.collect {
                  if (recordStatus == RecordStatus.RecordingInProgress) {
                    if (logcatSession.isRecording) {
                      recordStatus = RecordStatus.SaveRecordedLogs
                    } else {
                      recordStatus = RecordStatus.Idle
                    }
                  }
                }
              }

              db.filterDao().filters()
                .map { filters -> filters.filter { it.enabled } }
                .collectLatest { filters ->
                  appliedFilters = filters.isNotEmpty()
                  val infoMap = if (LogcatSession.isUidOptionSupported()) {
                    snapshotFlow { appInfoMap }.filterNotNull().first()
                  } else {
                    null
                  }
                  withContext(Dispatchers.Default) {
                    val includeFilters = filters.filterNot { it.exclude }
                    val excludeFilters = filters.filter { it.exclude }
                    logcatSession.setFilters(
                      filters = includeFilters.map { filterInfo ->
                        LogFilter(
                          filterInfo = filterInfo,
                          appInfoMap = infoMap,
                        )
                      },
                      exclusion = false
                    )
                    logcatSession.setFilters(
                      filters = excludeFilters.map { filterInfo ->
                        LogFilter(
                          filterInfo = filterInfo,
                          appInfoMap = infoMap,
                        )
                      },
                      exclusion = true
                    )
                  }

                  logsState.clear()
                  isLogcatSessionLoading = false
                  logcatSession.logs.collect { logs ->
                    logsState += logs
                  }
                }
            }
          } else {
            errorStartingLogcat = true
            isLogcatSessionLoading = false
          }
        }
    }
  }

  Scaffold(
    modifier = modifier,
    contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical),
    topBar = {
      val startedRecordingMessage = stringResource(R.string.started_recording)
      val saveFailedMessage = stringResource(R.string.failed_to_save_logs)

      if (logcatService != null) {
        LaunchedEffect(logcatService) {
          snapshotFlow { recordStatus }
            .map { it == RecordStatus.RecordingInProgress }
            .collect { isRecording ->
              logcatService.updateNotification(showStopRecording = isRecording)
            }
        }
      }

      LaunchedEffect(logcatService) {
        var lastShownSnackBar: Job? = null
        snapshotFlow { recordStatus }
          .collect { status ->
            when (status) {
              RecordStatus.Idle -> Unit
              RecordStatus.RecordingInProgress -> {
                val logcatSession = snapshotFlow { logcatService }.filterNotNull()
                  .mapNotNull { it.logcatSessionStatus.filterNotNull().first() }
                  .filterIsInstance<LogcatSessionStatus.Started>()
                  .map { it.session }
                  .first()

                if (!logcatSession.isRecording) {
                  val recordingFileInfo = createFileToStartRecording(context)
                  if (recordingFileInfo == null) {
                    context.showToast(context.getString(R.string.error))
                    recordStatus = RecordStatus.Idle
                    return@collect
                  }

                  val writer = recordingFileInfo.createBufferedWriter(context)
                  if (writer == null) {
                    context.showToast(context.getString(R.string.error))
                    recordStatus = RecordStatus.Idle
                    return@collect
                  }

                  logcatSession.startRecording(
                    recordingFileInfo = recordingFileInfo,
                    writer = writer,
                  )

                  lastShownSnackBar?.cancel()
                  lastShownSnackBar = launch {
                    snackbarHostState.showSnackbar(
                      message = startedRecordingMessage,
                      withDismissAction = true,
                      duration = SnackbarDuration.Short,
                    )
                  }
                }
              }
              RecordStatus.SaveRecordedLogs -> {
                val logcatSession = snapshotFlow { logcatService }.filterNotNull()
                  .mapNotNull { it.logcatSessionStatus.filterNotNull().first() }
                  .filterIsInstance<LogcatSessionStatus.Started>()
                  .map { it.session }
                  .first()
                val info = withContext(Dispatchers.IO) { logcatSession.stopRecording() }

                if (info != null) {
                  lastShownSnackBar?.cancel()
                  lastShownSnackBar = null
                  recordStatus = RecordStatus.Idle
                  savedLogsSheetState = SavedLogsBottomSheetState.Show(
                    fileName = info.fileName,
                    uri = info.uri,
                    isCustomLocation = info.isCustomLocation,
                  )
                } else {
                  recordStatus = RecordStatus.Idle
                  lastShownSnackBar?.cancel()
                  lastShownSnackBar = launch {
                    snackbarHostState.showSnackbar(
                      message = saveFailedMessage,
                      withDismissAction = true,
                      duration = SnackbarDuration.Short,
                    )
                  }
                }
              }
            }
          }
      }

      var saveLogsInProgress by remember { mutableStateOf(false) }

      AppBar(
        title = stringResource(R.string.device_logs),
        subtitle = logsState.size.toString(),
        filtered = appliedFilters,
        isPaused = logcatPaused,
        pauseEnabled = recordStatus == RecordStatus.Idle &&
          !isLogcatSessionLoading && !errorStartingLogcat,
        recordEnabled = !logcatPaused && logcatService != null &&
          recordStatus != RecordStatus.SaveRecordedLogs &&
          !isLogcatSessionLoading && !errorStartingLogcat,
        recordStatus = recordStatus,
        showDropDownMenu = showDropDownMenu,
        saveEnabled = logcatService != null && !isLogcatSessionLoading && !errorStartingLogcat
          && logsState.isNotEmpty(),
        saveLogsInProgress = saveLogsInProgress,
        restartLogcatEnabled = logcatService != null && recordStatus == RecordStatus.Idle,
        onClickSearch = {
          showSearchBar = true
        },
        onClickPause = {
          logcatPaused = !logcatPaused
        },
        onClickRecord = {
          when (recordStatus) {
            RecordStatus.Idle -> {
              recordStatus = RecordStatus.RecordingInProgress
            }
            RecordStatus.RecordingInProgress -> {
              recordStatus = RecordStatus.SaveRecordedLogs
            }
            RecordStatus.SaveRecordedLogs -> Unit
          }
        },
        onShowDropdownMenu = {
          showDropDownMenu = true
        },
        onDismissDropdownMenu = {
          showDropDownMenu = false
        },
        onClickClear = {
          logsState.clear()
          showDropDownMenu = false
        },
        onClickFilter = {
          showDropDownMenu = false
          val intent = Intent(context, FiltersActivity::class.java)
          context.startActivity(intent)
        },
        onClickDisplayOptions = {
          showDropDownMenu = false
          showDisplayOptions = true
        },
        onClickSave = {
          coroutineScope.launch {
            val logs = logsState.toList() // Create a copy
            if (logs.isNotEmpty()) {
              saveLogsToFile(context, logs).collect { result ->
                when (result) {
                  SaveResult.InProgress -> {
                    saveLogsInProgress = true
                  }
                  is SaveResult.Failure -> {
                    context.showToast(saveFailedMessage)
                    saveLogsInProgress = false
                    showDropDownMenu = false
                  }
                  is SaveResult.Success -> {
                    saveLogsInProgress = false
                    showDropDownMenu = false
                    savedLogsSheetState = SavedLogsBottomSheetState.Show(
                      fileName = result.fileName,
                      uri = result.uri,
                      isCustomLocation = result.isCustomLocation,
                    )
                  }
                }
              }
            }
          }
        },
        onClickSavedLogs = {
          showDropDownMenu = false
          context.startActivity(Intent(context, SavedLogsActivity::class.java))
        },
        onClickRestartLogcat = {
          showDropDownMenu = false
          if (logcatService != null) {
            isLogcatSessionLoading = true
            logcatService.restartLogcatSession()
          }
        },
        onClickSettings = {
          showDropDownMenu = false
          context.startActivity(Intent(context, SettingsActivity::class.java))
        },
      )
      AnimatedVisibility(
        visible = showSearchBar,
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        SearchLogsTopBar(
          searchQuery = searchQuery,
          searchInProgress = searchInProgress,
          showHitCount = showHitCount,
          hitCount = searchHitIndexMap.size,
          currentHitIndex = currentSearchHitIndex,
          onQueryChange = { searchQuery = it },
          onClose = {
            showSearchBar = false
            searchHitIndexMap.clear()
            searchHits = emptyList()
            currentSearchHitIndex = -1
            focusManager.clearFocus()
            searchQuery = ""
          },
          onPrevious = {
            focusManager.clearFocus()
            if (currentSearchHitIndex - 1 >= 0) {
              currentSearchHitIndex -= 1
            } else {
              currentSearchHitIndex = searchHitIndexMap.size - 1
            }
          },
          onNext = {
            focusManager.clearFocus()
            currentSearchHitIndex = (currentSearchHitIndex + 1) % searchHitIndexMap.size
          },
          regexEnabled = useRegexForSearch,
          regexError = searchRegexError,
          onClickRegex = {
            useRegexForSearch = !useRegexForSearch
            if (!useRegexForSearch) {
              searchRegexError = false
            }
          },
        )
      }
    },
    floatingActionButton = {
      FloatingActionButtons(
        visible = snapScrollInfo.isScrollSnapperVisible,
        scrollToTopInteractionSource = scrollToTopInteractionSource,
        scrollToBottomInteractionSource = scrollToBottomInteractionSource,
        onClickScrollToTop = {
          coroutineScope.launch {
            lazyListState.scrollToItem(0)
          }
        },
        onClickScrollToBottom = {
          coroutineScope.launch {
            if (lazyListState.layoutInfo.totalItemsCount > 0) {
              if (!showSearchBar || searchQuery.isEmpty()) {
                snapToBottom = true
              }
              lazyListState.scrollToItem(lazyListState.layoutInfo.totalItemsCount - 1)
            }
          }
        },
      )
    },
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    }
  ) { innerPadding ->
    if (showSearchBar) {
      LaunchedEffect(Unit) {
        snapshotFlow { Pair(searchQuery, useRegexForSearch) }
          .collectLatest { (searchQuery, useRegex) ->
            delay(100L)
            showHitCount = searchQuery.isNotEmpty()
            if (searchQuery.isNotEmpty()) {
              searchInProgress = true
              var scrolled = false
              val searchRegex = if (useRegex) {
                withContext(Dispatchers.Default) {
                  searchQuery.toRegexOrNull()
                }.also { searchRegex ->
                  searchRegexError = searchRegex == null
                }
              } else {
                null
              }
              snapshotFlow { logsState.toList() }
                .collect { logs ->
                  val (hitIndexMap, hits) = when {
                    useRegex && searchRegex == null -> {
                      SearchResult(hitIndexMap = emptyMap(), hits = emptyList())
                    }
                    searchRegex != null -> {
                      searchLogs(
                        logs = logs,
                        appInfoMap = appInfoMap.orEmpty(),
                        searchRegex = searchRegex,
                      )
                    }
                    else -> {
                      searchLogs(
                        logs = logs,
                        appInfoMap = appInfoMap.orEmpty(),
                        searchQuery = searchQuery,
                      )
                    }
                  }
                  searchHitIndexMap.clear()
                  searchHitIndexMap.putAll(hitIndexMap)
                  searchHits = hits

                  if (!scrolled) {
                    searchInProgress = false
                    if (searchHits.isNotEmpty()) {
                      currentSearchHitIndex = 0
                      snapToBottom = false
                      scrolled = true
                    } else {
                      currentSearchHitIndex = -1
                    }
                  }
                }
            } else {
              searchRegexError = false
              searchInProgress = false
              searchHitIndexMap.clear()
              searchHits = emptyList()
              currentSearchHitIndex = -1
            }
          }
      }
      if (searchQuery.isNotEmpty()) {
        LaunchedEffect(lazyListState, searchQuery) {
          snapshotFlow { searchHits to currentSearchHitIndex }
            .filter { (_, hitIndex) -> hitIndex != -1 }
            .distinctUntilChangedBy { (_, index) -> index }
            .collectLatest { (hits, hitIndex) ->
              if (hitIndex < hits.size) {
                val scrollIndex = hits[hitIndex].index
                if (scrollIndex != -1 && scrollIndex < lazyListState.layoutInfo.totalItemsCount) {
                  lazyListState.scrollToItem(scrollIndex)
                }
              }
            }
        }
      }
    }

    if (savedLogsSheetState is SavedLogsBottomSheetState.Show) {
      val saveInfo = savedLogsSheetState as SavedLogsBottomSheetState.Show
      SavedLogsBottomSheet(
        fileName = saveInfo.fileName,
        uri = saveInfo.uri,
        isCustomLocation = saveInfo.isCustomLocation,
        onDismiss = {
          savedLogsSheetState = SavedLogsBottomSheetState.Hide
        },
      )
    }

    MaybeShowPermissionRequiredDialog()

    if (isLogcatSessionLoading) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .consumeWindowInsets(innerPadding)
          .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(48.dp),
        )
      }
    } else if (errorStartingLogcat) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .consumeWindowInsets(innerPadding)
          .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            modifier = Modifier.size(32.dp),
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
          )
          Spacer(Modifier.height(8.dp))
          Text(
            text = stringResource(R.string.unable_to_start_logcat_error_msg),
            style = AppTypography.bodyMedium,
          )
        }
      }
    } else {
      var showCopyToClipboardSheet by remember { mutableStateOf<Log?>(null) }
      var showLongClickOptionsSheet by remember { mutableStateOf<Log?>(null) }

      if (logsState.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(innerPadding)
            .safeDrawingPadding(),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              modifier = Modifier.size(32.dp),
              imageVector = Icons.Default.Info,
              contentDescription = null,
            )
            Spacer(Modifier.height(8.dp))
            Text(
              text = stringResource(R.string.waiting_for_logs),
              style = AppTypography.bodyMedium,
            )
          }
        }
      } else {
        val lifecycle = LocalLifecycleOwner.current.lifecycle

        if (snapToBottom) {
          LaunchedEffect(lazyListState, compactViewPreference) {
            if (lazyListState.layoutInfo.totalItemsCount > 0) {
              lazyListState.scrollToItem(lazyListState.layoutInfo.totalItemsCount)
            }
          }
        }

        val enabledLogItems = remember(toggleableLogItemsPref.value) {
          toggleableLogItemsPref.value.orEmpty().map {
            ToggleableLogItem.entries[it.toInt()]
          }.toSet()
        }

        LogsList(
          modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(innerPadding)
            .pointerInput(Unit) {
              lifecycle.currentStateFlow.collectLatest { state ->
                if (state == Lifecycle.State.RESUMED) {
                  awaitPointerEventScope {
                    while (true) {
                      val event = awaitPointerEvent()
                      when (event.type) {
                        PointerEventType.Press -> {
                          snapToBottom = false
                          focusManager.clearFocus()
                        }
                      }
                    }
                  }
                }
              }
            },
          contentPadding = innerPadding,
          listStyle = if (!showSearchBar && compactViewPreference.value) {
            LogsListStyle.Compact
          } else {
            LogsListStyle.Default
          },
          enabledLogItems = enabledLogItems,
          logs = logsState,
          appInfoMap = appInfoMap.orEmpty(),
          searchHitIndexMap = searchHitIndexMap,
          searchHits = searchHits,
          onClick = if (!compactViewPreference.value) {
            { index ->
              showCopyToClipboardSheet = logsState[index]
              snapToBottom = false
            }
          } else null,
          onLongClick = { index ->
            showLongClickOptionsSheet = logsState[index]
            snapToBottom = false
          },
          state = lazyListState,
          currentSearchHitIndex = currentSearchHitIndex,
        )

        showCopyToClipboardSheet?.let { log ->
          CopyLogClipboardBottomSheet(
            log = log,
            onDismiss = { showCopyToClipboardSheet = null },
          )
        }
      }

      showLongClickOptionsSheet?.let { log ->
        val packageName = log.uid?.let { uid ->
          if (uid.isDigitsOnly()) {
            appInfoMap.orEmpty()[uid]?.packageName
          } else {
            uid
          }
        }
        LongClickOptionsSheet(
          showCopyToClipboard = compactViewPreference.value,
          onDismiss = { showLongClickOptionsSheet = null },
          onClickFilter = {
            val intent = Intent(context, FiltersActivity::class.java)
            intent.putExtra(FiltersActivity.EXTRA_LOG, log)
            intent.putExtra(FiltersActivity.EXTRA_PACKAGE_NAME, packageName)
            intent.putExtra(FiltersActivity.EXTRA_EXCLUDE, false)
            context.startActivity(intent)
            showLongClickOptionsSheet = null
          },
          onClickExclude = {
            val intent = Intent(context, FiltersActivity::class.java)
            intent.putExtra(FiltersActivity.EXTRA_LOG, log)
            intent.putExtra(FiltersActivity.EXTRA_PACKAGE_NAME, packageName)
            intent.putExtra(FiltersActivity.EXTRA_EXCLUDE, true)
            context.startActivity(intent)
            showLongClickOptionsSheet = null
          },
          onClickCopyToClipboard = {
            showCopyToClipboardSheet = log
            showLongClickOptionsSheet = null
          }
        )
      }

      if (showDisplayOptions) {
        DisplayOptionsSheet(
          initialEnabledLogcatItems = toggleableLogItemsPref.value.orEmpty().map {
            ToggleableLogItem.entries[it.toInt()]
          }.toSet(),
          initialCompactView = compactViewPreference.value,
          onSave = { enabledLogItems, compactView ->
            showDisplayOptions = false
            compactViewPreference.value = compactView
            toggleableLogItemsPref.value = enabledLogItems.map {
              it.ordinal.toString()
            }.toSet()
          },
          onDismiss = {
            showDisplayOptions = false
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DisplayOptionsSheet(
  initialEnabledLogcatItems: Set<ToggleableLogItem>,
  initialCompactView: Boolean,
  onSave: (enabledLogItems: Set<ToggleableLogItem>, compactView: Boolean) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ModalBottomSheet(
    modifier = modifier.statusBarsPadding(),
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
  ) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
      var compactView by remember { mutableStateOf(initialCompactView) }
      val enabledLogcatItems = remember {
        mutableStateMapOf(
          *ToggleableLogItem.entries.map { entry ->
            Pair(entry, entry in initialEnabledLogcatItems)
          }.toTypedArray()
        )
      }
      Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          modifier = Modifier.weight(1f),
          text = stringResource(R.string.display_options),
          style = AppTypography.headlineMedium,
        )
        Button(
          onClick = {
            onSave(
              enabledLogcatItems.filterValues { it }.keys,
              compactView,
            )
          },
        ) {
          Text(
            stringResource(R.string.save),
            style = AppTypography.titleMedium,
          )
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
      val uidSupported by LogcatSession.uidOptionSupported.collectAsState()
      FlowRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
      ) {
        ToggleableLogItem.entries
          .filter { item ->
            if (uidSupported == true) {
              true
            } else {
              item != ToggleableLogItem.PackageName
            }
          }
          .fastForEach { entry ->
            FilterChip(
              selected = enabledLogcatItems.getValue(entry),
              onClick = {
                enabledLogcatItems[entry] = !enabledLogcatItems.getValue(entry)
              },
              enabled = !compactView || entry == ToggleableLogItem.Tag,
              label = {
                Text(stringResource(entry.labelRes))
              }
            )
          }
      }
      ListItem(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            compactView = !compactView
          },
        leadingContent = {
          Icon(Icons.Default.ViewCompact, contentDescription = null)
        },
        headlineContent = {
          Text(stringResource(R.string.compact_view))
        },
        trailingContent = {
          Switch(
            checked = compactView,
            onCheckedChange = null,
          )
        },
        colors = ListItemDefaults.colors(
          containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
      )
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LongClickOptionsSheet(
  showCopyToClipboard: Boolean,
  onDismiss: () -> Unit,
  onClickFilter: () -> Unit,
  onClickExclude: () -> Unit,
  onClickCopyToClipboard: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ModalBottomSheet(
    modifier = modifier.statusBarsPadding(),
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
  ) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
      if (showCopyToClipboard) {
        ListItem(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              onClickCopyToClipboard()
            },
          headlineContent = {
            Text(stringResource(R.string.copy_to_clipboard))
          },
          colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
          ),
        )
      }
      ListItem(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            onClickFilter()
          },
        headlineContent = {
          Text(stringResource(R.string.filter))
        },
        colors = ListItemDefaults.colors(
          containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
      )
      ListItem(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            onClickExclude()
          },
        headlineContent = {
          Text(stringResource(R.string.exclude))
        },
        colors = ListItemDefaults.colors(
          containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedLogsBottomSheet(
  fileName: String,
  uri: Uri,
  isCustomLocation: Boolean,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  ModalBottomSheet(
    modifier = modifier.statusBarsPadding(),
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
  ) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
      ListItem(
        modifier = Modifier.fillMaxWidth(),
        headlineContent = {
          Text(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.saved_as_filename).format(fileName),
            style = AppTypography.titleMedium,
          )
        },
        colors = ListItemDefaults.colors(
          containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
      )
      ListItem(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            onDismiss()
            val intent = Intent(context, SavedLogsViewerActivity::class.java)
            intent.setDataAndType(uri, "text/plain")
            context.startActivity(intent)
          },
        leadingContent = {
          Icon(imageVector = Icons.AutoMirrored.Default.ViewList, contentDescription = null)
        },
        headlineContent = {
          Text(text = stringResource(R.string.view_log))
        },
        colors = ListItemDefaults.colors(
          containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
      )
      ListItem(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            onDismiss()
            ShareUtils.shareSavedLogs(
              context = context,
              uri = uri,
              isCustom = isCustomLocation,
            )
          },
        leadingContent = {
          Icon(imageVector = Icons.Default.Share, contentDescription = null)
        },
        headlineContent = {
          Text(text = stringResource(R.string.share))
        },
        colors = ListItemDefaults.colors(
          containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
      )
    }
  }
}

@Composable
private fun FloatingActionButtons(
  visible: Boolean,
  scrollToTopInteractionSource: MutableInteractionSource,
  scrollToBottomInteractionSource: MutableInteractionSource,
  onClickScrollToTop: () -> Unit,
  onClickScrollToBottom: () -> Unit,
) {
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
    exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
  ) {
    Column(
      modifier = Modifier
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
      FloatingActionButton(
        modifier = Modifier.size(48.dp),
        onClick = onClickScrollToTop,
        interactionSource = scrollToTopInteractionSource,
      ) {
        Icon(
          imageVector = Icons.Filled.ArrowUpward,
          contentDescription = null
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      FloatingActionButton(
        modifier = Modifier.size(48.dp),
        onClick = onClickScrollToBottom,
        interactionSource = scrollToBottomInteractionSource,
      ) {
        Icon(
          imageVector = Icons.Filled.ArrowDownward,
          contentDescription = null
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
  title: String,
  subtitle: String,
  filtered: Boolean,
  isPaused: Boolean,
  pauseEnabled: Boolean,
  recordEnabled: Boolean,
  recordStatus: RecordStatus,
  showDropDownMenu: Boolean,
  saveEnabled: Boolean,
  saveLogsInProgress: Boolean,
  restartLogcatEnabled: Boolean,
  onClickSearch: () -> Unit,
  onClickPause: () -> Unit,
  onClickRecord: () -> Unit,
  onShowDropdownMenu: () -> Unit,
  onDismissDropdownMenu: () -> Unit,
  onClickDisplayOptions: () -> Unit,
  onClickFilter: () -> Unit,
  onClickSave: () -> Unit,
  onClickSavedLogs: () -> Unit,
  onClickClear: () -> Unit,
  onClickRestartLogcat: () -> Unit,
  onClickSettings: () -> Unit,
) {
  TopAppBar(
    title = {
      Column(
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
      ) {
        Text(
          text = title,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (filtered) {
            Icon(
              modifier = Modifier.size(16.dp),
              imageVector = Icons.Default.FilterList,
              contentDescription = null,
            )
            Spacer(modifier = Modifier.width(4.dp))
          }
          Text(
            text = subtitle,
            style = AppTypography.titleSmall,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    actions = {
      Row(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.End))
      ) {
        WithTooltip(
          text = stringResource(R.string.search)
        ) {
          IconButton(
            onClick = onClickSearch,
            colors = IconButtonDefaults.iconButtonColors(
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
          ) {
            Icon(Icons.Default.Search, contentDescription = null)
          }
        }
        WithTooltip(
          text = if (isPaused) {
            stringResource(R.string.resume)
          } else {
            stringResource(R.string.pause)
          }
        ) {
          IconButton(
            onClick = onClickPause,
            enabled = pauseEnabled,
            colors = IconButtonDefaults.iconButtonColors(
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
          ) {
            if (isPaused) {
              Icon(Icons.Default.PlayArrow, contentDescription = null)
            } else {
              Icon(Icons.Default.Pause, contentDescription = null)
            }
          }
        }

        WithTooltip(
          text = when (recordStatus) {
            RecordStatus.Idle -> {
              stringResource(R.string.start_recording)
            }
            RecordStatus.RecordingInProgress -> {
              stringResource(R.string.stop_recording)
            }
            RecordStatus.SaveRecordedLogs -> {
              stringResource(R.string.saving_recorded_logs)
            }
          }
        ) {
          IconButton(
            onClick = onClickRecord,
            enabled = recordEnabled,
            colors = IconButtonDefaults.iconButtonColors(
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
          ) {
            when (recordStatus) {
              RecordStatus.Idle -> {
                Icon(Icons.Default.FiberManualRecord, contentDescription = null)
              }
              RecordStatus.RecordingInProgress -> {
                Icon(Icons.Default.Stop, contentDescription = null)
              }
              RecordStatus.SaveRecordedLogs -> {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  strokeWidth = 2.dp,
                )
              }
            }
          }
        }
        Box {
          WithTooltip(
            text = stringResource(R.string.more_options),
          ) {
            IconButton(
              onClick = onShowDropdownMenu,
              colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
            ) {
              Icon(Icons.Default.MoreVert, contentDescription = null)
            }
          }
          DropdownMenu(
            expanded = showDropDownMenu,
            onDismissRequest = onDismissDropdownMenu,
          ) {
            DropdownMenuItem(
              leadingIcon = {
                Icon(Icons.Default.DisplaySettings, contentDescription = null)
              },
              text = {
                Text(
                  text = stringResource(R.string.display_options),
                )
              },
              onClick = onClickDisplayOptions,
            )
            DropdownMenuItem(
              leadingIcon = {
                Icon(Icons.Default.FilterList, contentDescription = null)
              },
              text = {
                Text(
                  text = stringResource(R.string.filters),
                )
              },
              onClick = onClickFilter,
            )
            DropdownMenuItem(
              leadingIcon = {
                if (saveLogsInProgress) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                  )
                } else {
                  Icon(Icons.Default.Save, contentDescription = null)
                }
              },
              text = {
                Text(
                  text = stringResource(R.string.save),
                )
              },
              onClick = onClickSave,
              enabled = saveEnabled,
            )
            DropdownMenuItem(
              leadingIcon = {
                Icon(Icons.Default.Folder, contentDescription = null)
              },
              text = {
                Text(
                  text = stringResource(R.string.saved_logs),
                )
              },
              onClick = onClickSavedLogs,
            )
            DropdownMenuItem(
              leadingIcon = {
                Icon(Icons.Default.Clear, contentDescription = null)
              },
              text = {
                Text(
                  text = stringResource(R.string.clear),
                )
              },
              onClick = onClickClear,
            )
            DropdownMenuItem(
              leadingIcon = {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
              },
              text = {
                Text(
                  text = stringResource(R.string.restart_logcat),
                )
              },
              onClick = onClickRestartLogcat,
              enabled = restartLogcatEnabled,
            )
            DropdownMenuItem(
              leadingIcon = {
                Icon(Icons.Default.Settings, contentDescription = null)
              },
              text = {
                Text(
                  text = stringResource(R.string.settings),
                )
              },
              onClick = onClickSettings,
            )
          }
        }
      }
    }
  )
}

@Composable
private fun rememberLogcatServiceConnection(): LogcatService? {
  var logcatService by remember { mutableStateOf<LogcatService?>(null) }
  val context = LocalContext.current
  DisposableEffect(context) {
    val serviceConnection = object : ServiceConnection {
      override fun onServiceConnected(
        name: ComponentName?,
        service: IBinder,
      ) {
        Logger.debug(TAG, "LogcatService - onServiceConnected")
        logcatService = service.getService()
      }

      override fun onServiceDisconnected(name: ComponentName) {
        Logger.debug(TAG, "LogcatService - onServiceDisconnected")
        logcatService = null
      }
    }

    Logger.debug(TAG, "LogcatService - bind")
    context.bindService(
      Intent(context, LogcatService::class.java),
      serviceConnection,
      Context.BIND_ABOVE_CLIENT,
    )

    onDispose {
      Logger.debug(TAG, "LogcatService - unbind")
      context.unbindService(serviceConnection)
      logcatService = null
    }
  }

  return logcatService
}

data class SnapScrollInfo(
  val isScrollSnapperVisible: Boolean = false,
)

@Composable
private fun rememberSnapScrollInfo(
  lazyListState: LazyListState,
  snapToBottom: Boolean,
  snapUpInteractionSource: InteractionSource,
  snapDownInteractionSource: InteractionSource,
): SnapScrollInfo {
  var snapScrollInfo by remember { mutableStateOf(SnapScrollInfo()) }

  if (snapToBottom) {
    LaunchedEffect(lazyListState) {
      snapScrollInfo = SnapScrollInfo()
      snapshotFlow { lazyListState.layoutInfo.totalItemsCount }
        .filter { lastIndex -> lastIndex > 0 }
        .collect { lastIndex ->
          lazyListState.scrollToItem(lastIndex)
        }
    }
  } else {
    LaunchedEffect(lazyListState, snapUpInteractionSource, snapDownInteractionSource) {
      data class LastItemOffsetInfo(
        val lastItem: Boolean,
        val lastItemSize: Int,
        val lastVisibleOffset: Int,
      )

      data class ItemOffsetInfo(
        val viewportEndOffset: Int,
        val firstVisibleIndex: Int,
        val firstVisibleOffset: Int,
        val lastItemInfo: LastItemOffsetInfo?,
        val lastScrolledForward: Boolean,
        val lastScrolledBackward: Boolean,
      )

      launch {
        combine(
          snapshotFlow {
            val layoutInfo = lazyListState.layoutInfo
            ItemOffsetInfo(
              viewportEndOffset = layoutInfo.viewportEndOffset,
              firstVisibleIndex = lazyListState.firstVisibleItemIndex,
              firstVisibleOffset = lazyListState.firstVisibleItemScrollOffset,
              lastItemInfo = layoutInfo.visibleItemsInfo.lastOrNull()?.let { info ->
                LastItemOffsetInfo(
                  lastItem = info.index == layoutInfo.totalItemsCount - 1,
                  lastItemSize = info.size,
                  lastVisibleOffset = info.offset,
                )
              },
              lastScrolledForward = lazyListState.lastScrolledForward,
              lastScrolledBackward = lazyListState.lastScrolledBackward,
            )
          },
          snapUpInteractionSource.interactions.stateIn(this, Eagerly, null),
          snapDownInteractionSource.interactions.stateIn(this, Eagerly, null),
        ) { offsetInfo, snapUpInteraction, snapDownInteraction ->
          Triple(offsetInfo, snapUpInteraction, snapDownInteraction)
        }.collectLatest { (offsetInfo, snapUpInteraction, snapDownInteraction) ->
          var shouldSnapScrollUp = false
          var shouldSnapScrollDown = false
          if (offsetInfo.lastScrolledForward) {
            val lastItemInfo = offsetInfo.lastItemInfo
            if (lastItemInfo != null) {
              val canScrollDown = !lastItemInfo.lastItem ||
                (lastItemInfo.lastVisibleOffset + lastItemInfo.lastItemSize) > offsetInfo.viewportEndOffset
              shouldSnapScrollUp = false
              shouldSnapScrollDown = canScrollDown
            } else {
              shouldSnapScrollDown = false
            }
          } else if (offsetInfo.lastScrolledBackward) {
            val canScrollUp =
              offsetInfo.firstVisibleIndex != 0 || offsetInfo.firstVisibleOffset > 0
            shouldSnapScrollDown = false
            shouldSnapScrollUp = canScrollUp
          }
          var isScrollSnapperVisible = shouldSnapScrollUp || shouldSnapScrollDown
          snapScrollInfo = snapScrollInfo.copy(
            isScrollSnapperVisible = isScrollSnapperVisible,
          )

          // Do not hide while the FABs are being pressed.
          val isFabPressed = snapUpInteraction is Press || snapDownInteraction is Press
          if (isScrollSnapperVisible && !isFabPressed) {
            delay(SNAP_SCROLL_HIDE_DELAY_MS)
            snapScrollInfo = snapScrollInfo.copy(
              isScrollSnapperVisible = false,
            )
          }
        }
      }
    }
  }
  return snapScrollInfo
}

sealed interface SaveResult {
  data object InProgress : SaveResult
  data class Success(
    val fileName: String,
    val uri: Uri,
    val isCustomLocation: Boolean,
  ) : SaveResult

  data object Failure : SaveResult
}

private suspend fun createFileToStartRecording(context: Context): RecordingFileInfo? {
  val createFileResult = withContext(Dispatchers.IO) {
    createFile(context = context, recording = true)
  }

  if (createFileResult == null) return null

  val (uri, isCustomLocation, timestamp) = createFileResult
  val fileName = if (isCustomLocation) {
    DocumentFile.fromSingleUri(context, uri)?.name
  } else {
    uri.toFile().name
  }

  if (fileName == null) return null

  val db = LogcatReaderDatabase.getInstance(context)
  withContext(Dispatchers.IO) {
    db.savedLogsDao().insert(
      SavedLogInfo(
        fileName = fileName,
        path = uri.toString(),
        isCustom = isCustomLocation,
        timestamp = timestamp,
      )
    )
  }

  return RecordingFileInfo(
    fileName = fileName,
    uri = uri,
    isCustomLocation = isCustomLocation
  )
}

private suspend fun RecordingFileInfo.createBufferedWriter(
  context: Context
): BufferedWriter? = withContext(Dispatchers.IO) {
  try {
    if (isCustomLocation) {
      context.contentResolver.openOutputStream(uri)?.bufferedWriter()
    } else {
      uri.toFile().bufferedWriter()
    }
  } catch (_: IOException) {
    null
  }
}

private fun saveLogsToFile(context: Context, logs: List<Log>): Flow<SaveResult> = flow {
  emit(SaveResult.InProgress)
  check(logs.isNotEmpty()) { "logs list is empty" }

  val createFileResult = withContext(Dispatchers.IO) { createFile(context) }
  if (createFileResult == null) {
    emit(SaveResult.Failure)
    return@flow
  }

  val (uri, isCustomLocation, timestamp) = createFileResult
  val success = withContext(Dispatchers.IO) {
    if (isCustomLocation) {
      LogcatUtil.writeToFile(context, logs, uri)
    } else {
      LogcatUtil.writeToFile(logs, uri.toFile())
    }
  }

  if (success) {
    val fileName = if (isCustomLocation) {
      DocumentFile.fromSingleUri(context, uri)?.name
    } else {
      uri.toFile().name
    }

    if (fileName == null) {
      emit(SaveResult.Failure)
      return@flow
    }

    val db = LogcatReaderDatabase.getInstance(context)
    withContext(Dispatchers.IO) {
      db.savedLogsDao().insert(
        SavedLogInfo(
          fileName = fileName,
          path = uri.toString(),
          isCustom = isCustomLocation,
          timestamp = timestamp
        )
      )
    }

    emit(
      SaveResult.Success(
        fileName = fileName,
        uri = uri,
        isCustomLocation = isCustomLocation,
      )
    )
  } else {
    emit(SaveResult.Failure)
  }
}

private data class CreateFileResult(
  val uri: Uri,
  val isCustom: Boolean,
  val timestamp: Long,
)

@WorkerThread
private fun createFile(context: Context, recording: Boolean = false): CreateFileResult? {
  val date = Date()
  val timeStamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    .format(date)
  val fileName = buildString {
    append("logcat_")
    if (recording) {
      append("recording_")
    }
    append(timeStamp)
  }

  val customSaveLocation = context.getDefaultSharedPreferences().getString(
    SettingsPrefKeys.Logcat.KEY_SAVE_LOCATION,
    SettingsPrefKeys.Logcat.Default.SAVE_LOCATION
  )!!

  return if (customSaveLocation.isEmpty()) {
    val file = File(context.filesDir, LOGCAT_DIR)
    file.mkdirs()
    CreateFileResult(
      uri = File(file, "$fileName.txt").toUri(),
      isCustom = false,
      timestamp = date.time,
    )
  } else {
    val documentFile = DocumentFile.fromTreeUri(context, customSaveLocation.toUri())
    val uri = documentFile?.createFile("text/plain", fileName)?.uri
    if (uri == null) {
      return null
    }
    CreateFileResult(
      uri = uri,
      isCustom = true,
      timestamp = date.time,
    )
  }
}

enum class RecordStatus {
  Idle,
  RecordingInProgress,
  SaveRecordedLogs,
}

sealed interface SavedLogsBottomSheetState {
  data object Hide : SavedLogsBottomSheetState
  data class Show(
    val fileName: String,
    val uri: Uri,
    val isCustomLocation: Boolean,
  ) : SavedLogsBottomSheetState
}

private class LogFilter(
  private val filterInfo: FilterInfo,
  private val appInfoMap: Map<String, AppInfo>?,
) : Filter {
  private val regexEnabledTypes = filterInfo.regexFilterTypes
  private val messageRegex = filterInfo.message?.let { text ->
    if (RegexFilterType.Message in regexEnabledTypes) {
      text.toRegex()
    } else {
      null
    }
  }
  private val tagRegex = filterInfo.tag?.let { text ->
    if (RegexFilterType.Tag in regexEnabledTypes) {
      text.toRegex()
    } else {
      null
    }
  }
  private val packageNameRegex = filterInfo.packageName?.let { text ->
    if (RegexFilterType.PackageName in regexEnabledTypes) {
      text.toRegex()
    } else {
      null
    }
  }
  private val priorities: Set<String> = if (!filterInfo.logLevels.isNullOrEmpty()) {
    filterInfo.logLevels.split(",").toSet()
  } else {
    emptySet()
  }

  private fun matches(regex: Regex?, target: String): Boolean {
    return if (regex == null) {
      true
    } else {
      regex.matches(target)
    }
  }

  private fun matches(keyword: String?, target: String): Boolean {
    return if (keyword.isNullOrEmpty()) {
      true
    } else {
      target.contains(keyword, ignoreCase = true)
    }
  }

  private fun matchesPriority(log: Log): Boolean {
    return if (priorities.isEmpty()) {
      true
    } else {
      log.priority in priorities
    }
  }

  private fun matchesPackageName(log: Log): Boolean {
    if (appInfoMap == null) {
      return true
    }

    val packageName = filterInfo.packageName
    if (packageName == null) {
      return true
    }

    val uid = log.uid
    if (uid == null) {
      return false
    }

    if (!uid.isDigitsOnly()) {
      if (packageNameRegex != null) {
        return packageNameRegex.matches(uid)
      } else {
        return uid.contains(packageName, ignoreCase = true)
      }
    }

    return appInfoMap[log.uid]?.packageName.orEmpty().let { it ->
      if (packageNameRegex != null) {
        packageNameRegex.matches(it)
      } else {
        it.contains(packageName, ignoreCase = true)
      }
    }
  }

  override fun apply(log: Log): Boolean {
    if (tagRegex != null) {
      if (!matches(regex = tagRegex, target = log.tag)) {
        return false
      }
    } else {
      if (!matches(keyword = filterInfo.tag, target = log.tag)) {
        return false
      }
    }

    if (messageRegex != null) {
      if (!matches(regex = messageRegex, target = log.msg)) {
        return false
      }
    } else {
      if (!matches(keyword = filterInfo.message, target = log.msg)) {
        return false
      }
    }

    if (!matchesPackageName(log)) {
      return false
    }

    if (!matches(keyword = filterInfo.pid?.toString(), target = log.pid)) {
      return false
    }

    if (!matches(keyword = filterInfo.tid?.toString(), target = log.tid)) {
      return false
    }

    if (!matchesPriority(log)) {
      return false
    }

    return true
  }
}

class DeviceLogsViewModel(
  application: Application,
) : AndroidViewModel(application) {

  private val context: Context
    get() = getApplication<LogcatApp>().applicationContext

  private val _logcatService = mutableStateOf<LogcatService?>(null)
  val logcatService by _logcatService

  var recordStatus = mutableStateOf<RecordStatus>(RecordStatus.Idle)

  private val serviceConnection = object : ServiceConnection {
    override fun onServiceConnected(
      name: ComponentName?,
      service: IBinder,
    ) {
      Logger.debug(TAG, "LogcatService - onServiceConnected")
      _logcatService.value = service.getService()
    }

    override fun onServiceDisconnected(name: ComponentName) {
      Logger.debug(TAG, "LogcatService - onServiceDisconnected")
      _logcatService.value = null
    }
  }

  init {
    application.bindService(
      Intent(application, LogcatService::class.java),
      serviceConnection,
      Context.BIND_ABOVE_CLIENT,
    )
  }

  override fun onCleared() {
    // Stop the service if recording is not active.
    if (recordStatus.value == RecordStatus.Idle) {
      Logger.debug(TAG, "LogcatService - stopping service")
      context.stopService(Intent(context, LogcatService::class.java))
    }
  }
}
