package com.aesean.activitystack.utils

object IInputConnectionWrapper {
    private const val DO_GET_TEXT_AFTER_CURSOR = 10
    private const val DO_GET_TEXT_BEFORE_CURSOR = 20
    private const val DO_GET_SELECTED_TEXT = 25
    private const val DO_GET_CURSOR_CAPS_MODE = 30
    private const val DO_GET_EXTRACTED_TEXT = 40
    private const val DO_COMMIT_TEXT = 50
    private const val DO_COMMIT_COMPLETION = 55
    private const val DO_COMMIT_CORRECTION = 56
    private const val DO_SET_SELECTION = 57
    private const val DO_PERFORM_EDITOR_ACTION = 58
    private const val DO_PERFORM_CONTEXT_MENU_ACTION = 59
    private const val DO_SET_COMPOSING_TEXT = 60
    private const val DO_SET_COMPOSING_REGION = 63
    private const val DO_FINISH_COMPOSING_TEXT = 65
    private const val DO_SEND_KEY_EVENT = 70
    private const val DO_DELETE_SURROUNDING_TEXT = 80
    private const val DO_DELETE_SURROUNDING_TEXT_IN_CODE_POINTS = 81
    private const val DO_BEGIN_BATCH_EDIT = 90
    private const val DO_END_BATCH_EDIT = 95
    private const val DO_PERFORM_PRIVATE_COMMAND = 120
    private const val DO_CLEAR_META_KEY_STATES = 130
    private const val DO_REQUEST_UPDATE_CURSOR_ANCHOR_INFO = 140
    private const val DO_CLOSE_CONNECTION = 150
    private const val DO_COMMIT_CONTENT = 160
}

object ViewRootHandler {
    private const val MSG_INVALIDATE = 1
    private const val MSG_INVALIDATE_RECT = 2
    private const val MSG_DIE = 3
    private const val MSG_RESIZED = 4
    private const val MSG_RESIZED_REPORT = 5
    private const val MSG_WINDOW_FOCUS_CHANGED = 6
    private const val MSG_DISPATCH_INPUT_EVENT = 7
    private const val MSG_DISPATCH_APP_VISIBILITY = 8
    private const val MSG_DISPATCH_GET_NEW_SURFACE = 9
    private const val MSG_DISPATCH_KEY_FROM_IME = 11
    private const val MSG_DISPATCH_KEY_FROM_AUTOFILL = 12
    private const val MSG_CHECK_FOCUS = 13
    private const val MSG_CLOSE_SYSTEM_DIALOGS = 14
    private const val MSG_DISPATCH_DRAG_EVENT = 15
    private const val MSG_DISPATCH_DRAG_LOCATION_EVENT = 16
    private const val MSG_DISPATCH_SYSTEM_UI_VISIBILITY = 17
    private const val MSG_UPDATE_CONFIGURATION = 18
    private const val MSG_PROCESS_INPUT_EVENTS = 19
    private const val MSG_CLEAR_ACCESSIBILITY_FOCUS_HOST = 21
    private const val MSG_INVALIDATE_WORLD = 22
    private const val MSG_WINDOW_MOVED = 23
    private const val MSG_SYNTHESIZE_INPUT_EVENT = 24
    private const val MSG_DISPATCH_WINDOW_SHOWN = 25
    private const val MSG_REQUEST_KEYBOARD_SHORTCUTS = 26
    private const val MSG_UPDATE_POINTER_ICON = 27
    private const val MSG_POINTER_CAPTURE_CHANGED = 28
    private const val MSG_DRAW_FINISHED = 29
}

object ActivityThreadH {
    private const val LAUNCH_ACTIVITY = 100
    private const val PAUSE_ACTIVITY = 101
    private const val PAUSE_ACTIVITY_FINISHING = 102
    private const val STOP_ACTIVITY_SHOW = 103
    private const val STOP_ACTIVITY_HIDE = 104
    private const val SHOW_WINDOW = 105
    private const val HIDE_WINDOW = 106
    private const val RESUME_ACTIVITY = 107
    private const val SEND_RESULT = 108
    private const val DESTROY_ACTIVITY = 109

    private const val BIND_APPLICATION = 110
    private const val EXIT_APPLICATION = 111
    private const val RECEIVER = 113
    private const val CREATE_SERVICE = 114
    private const val SERVICE_ARGS = 115
    private const val STOP_SERVICE = 116

    private const val CONFIGURATION_CHANGED = 118
    private const val CLEAN_UP_CONTEXT = 119
    private const val GC_WHEN_IDLE = 120
    private const val BIND_SERVICE = 121
    private const val UNBIND_SERVICE = 122
    private const val DUMP_SERVICE = 123
    private const val LOW_MEMORY = 124
    private const val PROFILER_CONTROL = 127
    private const val CREATE_BACKUP_AGENT = 128
    private const val DESTROY_BACKUP_AGENT = 129
    private const val SUICIDE = 130
    private const val REMOVE_PROVIDER = 131
    private const val ENABLE_JIT = 132
    private const val DISPATCH_PACKAGE_BROADCAST = 133
    private const val SCHEDULE_CRASH = 134
    private const val DUMP_HEAP = 135
    private const val DUMP_ACTIVITY = 136
    private const val SLEEPING = 137
    private const val SET_CORE_SETTINGS = 138
    private const val UPDATE_PACKAGE_COMPATIBILITY_INFO = 139
    private const val DUMP_PROVIDER = 141
    private const val UNSTABLE_PROVIDER_DIED = 142
    private const val REQUEST_ASSIST_CONTEXT_EXTRAS = 143
    private const val TRANSLUCENT_CONVERSION_COMPLETE = 144
    private const val INSTALL_PROVIDER = 145
    private const val ON_NEW_ACTIVITY_OPTIONS = 146
    private const val ENTER_ANIMATION_COMPLETE = 149
    private const val START_BINDER_TRACKING = 150
    private const val STOP_BINDER_TRACKING_AND_DUMP = 151
    private const val LOCAL_VOICE_INTERACTION_STARTED = 154
    private const val ATTACH_AGENT = 155
    private const val APPLICATION_INFO_CHANGED = 156
    private const val RUN_ISOLATED_ENTRY_POINT = 158
    private const val EXECUTE_TRANSACTION = 159
    private const val RELAUNCH_ACTIVITY = 160
}

object InputMethodManagerH {
    private const val MSG_DUMP = 1
    private const val MSG_BIND = 2
    private const val MSG_UNBIND = 3
    private const val MSG_SET_ACTIVE = 4
    private const val MSG_SEND_INPUT_EVENT = 5
    private const val MSG_TIMEOUT_INPUT_EVENT = 6
    private const val MSG_FLUSH_INPUT_EVENT = 7
    private const val MSG_SET_USER_ACTION_NOTIFICATION_SEQUENCE_NUMBER = 9
    private const val MSG_REPORT_FULLSCREEN_MODE = 10
}

object FrameHandlerH {
    private const val MSG_DO_FRAME = 0
    private const val MSG_DO_SCHEDULE_VSYNC = 1
    private const val MSG_DO_SCHEDULE_CALLBACK = 2
}

object DisplayListenerDelegateH {
    private const val EVENT_DISPLAY_ADDED = 1
    private const val EVENT_DISPLAY_CHANGED = 2
    private const val EVENT_DISPLAY_REMOVED = 3
}
