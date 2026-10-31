package tv.withaibuild.customiuizer.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import tv.withaibuild.customiuizer.R
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Native input shared by home and app selection; focus belongs to the visible page. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsSearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier,
    requestFocus: Boolean = false, onClose: (() -> Unit)? = null,
) {
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val imeVisible = WindowInsets.isImeVisible
    var laidOut by remember { mutableStateOf(false) }
    var wasImeVisible by remember { mutableStateOf(false) }
    LaunchedEffect(imeVisible) {
        if (wasImeVisible && !imeVisible) focusManager.clearFocus(force = true)
        wasImeVisible = imeVisible
    }
    LaunchedEffect(requestFocus, laidOut) {
        if (!laidOut) return@LaunchedEffect
        if (requestFocus) focus.requestFocus() else {
            // Fragment restores its View hierarchy after the first composition is attached.
            withFrameNanos { }
            focusManager.clearFocus(force = true)
        }
    }
    DisposableEffect(lifecycle, focusManager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) focusManager.clearFocus(force = true)
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            focusManager.clearFocus(force = true)
        }
    }
    val description = stringResource(R.string.search_input_description)
    OutlinedTextField(query, onQueryChange, modifier.focusRequester(focus).onGloballyPositioned { laidOut = true }
        .semantics { contentDescription = description }, singleLine = true,
        placeholder = { Text(stringResource(androidx.appcompat.R.string.abc_search_hint)) },
        leadingIcon = {
            if (onClose != null) IconButton(onClick = onClose) {
                Icon(painterResource(androidx.appcompat.R.drawable.abc_ic_ab_back_material),
                    stringResource(androidx.appcompat.R.string.abc_toolbar_collapse_description))
            } else Icon(painterResource(androidx.appcompat.R.drawable.abc_ic_search_api_material), null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) {
                Icon(painterResource(androidx.appcompat.R.drawable.abc_ic_clear_material),
                    stringResource(androidx.appcompat.R.string.abc_searchview_description_clear))
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus(force = true) }))
}
