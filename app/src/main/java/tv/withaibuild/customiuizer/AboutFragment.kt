package tv.withaibuild.customiuizer

import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import tv.withaibuild.customiuizer.ui.SettingsActionRow
import tv.withaibuild.customiuizer.ui.SettingsSection
import tv.withaibuild.customiuizer.ui.settingsView
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class AboutFragment : Fragment() {

    private var donateDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = settingsView {
        val context = LocalContext.current
        val resources = androidx.compose.ui.platform.LocalResources.current
        val configuration = LocalConfiguration.current
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                        val icon = remember(resources, configuration) { resources.getDrawable(R.mipmap.ic_launcher, context.theme)?.toBitmap(128, 128) }
                        icon?.let { Image(it.asImageBitmap(), stringResource(R.string.app_name_full), Modifier.size(72.dp)) }
                    }
                    Text(stringResource(R.string.app_name_full), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.about_maintainer), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.about_based_on), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                SettingsSection(stringResource(R.string.about_links_category))
                Card(shape = RoundedCornerShape(20.dp)) {
                    SettingsActionRow(stringResource(R.string.about_repository_title), stringResource(R.string.about_repository_summary),
                        tag = "about_repository") { openLink(REPOSITORY_URL) }
                    SettingsActionRow(stringResource(R.string.about_community_title), BuildConfig.COMMUNITY_URL.removePrefix("https://"),
                        tag = "about_community") { openLink(BuildConfig.COMMUNITY_URL) }
                }
            }
            item {
                SettingsSection(stringResource(R.string.about_support_category))
                Card(shape = RoundedCornerShape(20.dp)) {
                    SettingsActionRow(stringResource(R.string.about_donate_title), stringResource(R.string.about_donate_summary),
                        tag = "about_donate") { showDonationDialog() }
                    SettingsActionRow(stringResource(R.string.about_paypal_title), stringResource(R.string.about_paypal_summary),
                        tag = "about_paypal") { openLink(PAYPAL_DONATION_URL) }
                }
            }
            item {
                SettingsSection(stringResource(R.string.about_notes_category))
                Card(shape = RoundedCornerShape(20.dp)) {
                    SettingsActionRow(stringResource(R.string.about_restart_notes_title), stringResource(R.string.about_dynamic), tag = "about_restart_notes")
                    SettingsActionRow(stringResource(R.string.about_compatibility_notes_title), stringResource(R.string.about_unsupported), tag = "about_compatibility_notes")
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActionBar()
        (activity as? MainActivity)?.settingsBar?.bind(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        donateDialog?.dismiss()
        donateDialog = null
    }

    private fun initActionBar() {
        val act = activity as? AppCompatActivity ?: return
        (act as? MainActivity)?.settingsBar?.apply {
            setTitle(R.string.app_about)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun showDonationDialog() {
        val context = context ?: return
        val bitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.wechat_donation_code,
            BitmapFactory.Options().apply {
                inScaled = false
                inSampleSize = DONATION_IMAGE_SAMPLE_SIZE
            }
        ) ?: run {
            showToast(R.string.about_donation_unavailable)
            return
        }

        val density = resources.displayMetrics.density
        val padding = (16f * density + 0.5f).toInt()
        val size = minOf(
            (300f * density + 0.5f).toInt(),
            resources.displayMetrics.widthPixels - padding * 2
        )

        val image = ImageView(context).apply {
            contentDescription = getString(R.string.about_donate_image_description)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageBitmap(bitmap)
        }
        val container = FrameLayout(context).apply {
            setPadding(padding, padding / 2, padding, 0)
            addView(image, FrameLayout.LayoutParams(size, size, Gravity.CENTER))
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.about_donate_title)
            .setView(container)
            .setPositiveButton(android.R.string.ok, null)
            .setOnDismissListener {
                image.setImageDrawable(null)
                if (!bitmap.isRecycled) bitmap.recycle()
                donateDialog = null
            }
            .create()

        donateDialog = dialog
        dialog.show()
    }

    private fun openLink(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            showToast(R.string.about_link_unavailable)
        }
    }

    private fun showToast(messageRes: Int) {
        val context = context ?: return
        Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        const val REPOSITORY_URL = "https://github.com/thetvplus/customiuizer-a14"
        const val PAYPAL_DONATION_URL = "https://paypal.me/Jinjitv"
        const val DONATION_IMAGE_SAMPLE_SIZE = 2
    }
}
