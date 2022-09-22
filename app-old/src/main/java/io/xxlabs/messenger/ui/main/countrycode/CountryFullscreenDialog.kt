package io.xxlabs.messenger.ui.main.countrycode

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.support.extensions.afterTextChanged
import io.xxlabs.messenger.support.extensions.setInsets

class CountryFullscreenDialog constructor(
    val countrySelectionListener: CountrySelectionListener
) : DialogFragment() {
    lateinit var root: View
    lateinit var countryRecyclerView: RecyclerView
    lateinit var searchInput: TextInputEditText
    lateinit var backBtn: ImageView

    init {
        isCancelable = true
    }

    @Nullable
    override fun onCreateView(
        @NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.component_country_fullscreen, container, false)
        root.setInsets(bottomMask = WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.ime())
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.XxFullscreenDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            WindowCompat.setDecorFitsSystemWindows(dialog.window!!, false)
            bindListeners()
            addCountryCodeList()
        }
        return dialog
    }

    private fun bindListeners() {
        backBtn.setOnClickListener {
            dismiss()
            countrySelectionListener.onDismiss()
        }

        searchInput.afterTextChanged {
            (countryRecyclerView.adapter as CountryCodeAdapter).filter(it)
        }
    }

    private fun addCountryCodeList() {
        val countriesList = Country.countriesList
        val adapter = CountryCodeAdapter(countriesList, countrySelectionListener)
        val layoutManager = LinearLayoutManager(requireContext())

        countryRecyclerView.layoutManager = layoutManager
        countryRecyclerView.setHasFixedSize(true)
        countryRecyclerView.adapter = adapter

        val itemDecoration =
            DividerItemDecoration(context, layoutManager.orientation)
        val color = ContextCompat.getColor(requireContext(), R.color.toolbarBarColor)
        val drawable = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            intArrayOf(color, color)
        )
        drawable.setSize(1, 1)
        itemDecoration.setDrawable(drawable)

        countryRecyclerView.addItemDecoration(itemDecoration)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        countryRecyclerView = view.findViewById(R.id.countryRecyclerView)
        searchInput = view.findViewById(R.id.countrySearchInput)
        backBtn = view.findViewById(R.id.toolbarGenericBackBtn)
        view.findViewById<TextView>(R.id.toolbarGenericTitle).text = "Country Code"
        val toolbar = view.findViewById<ViewGroup>(R.id.toolbarGeneric)
        toolbar.setInsets(topMask = WindowInsetsCompat.Type.systemBars())
    }

    companion object {
        fun getInstance(countrySelectionListener: CountrySelectionListener): CountryFullscreenDialog {
            return CountryFullscreenDialog(
                countrySelectionListener
            )
        }
    }
}