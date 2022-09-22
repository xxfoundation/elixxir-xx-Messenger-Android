package io.xxlabs.messenger.ui.main.countrycode

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.Country


class CountryCodeAdapter(
    private val countriesList: List<Country>,
    private val countrySelectionListener: CountrySelectionListener
) : RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder>(), Filterable {
    private var countriesFiltered: List<*> = countriesList
    private var lastSearch = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryCodeViewHolder {
        val context = parent.context
        val view =
            LayoutInflater.from(context).inflate(R.layout.list_item_country_code, parent, false)
        return CountryCodeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return countriesFiltered.size
    }

    override fun onBindViewHolder(holder: CountryCodeViewHolder, position: Int) {
        val item = countriesFiltered[position] as Country

        holder.countryFlag.text = item.flag
        holder.countryName.text = item.countryName
        holder.countryCode.text = item.dialCode
        holder.itemView.setOnClickListener {
            countrySelectionListener.onItemSelected(item)
        }

        holder.itemView.contentDescription =  "dialog.country.code.list.item.$position"
    }

    fun filter(text: String) {
        filter.filter(text)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val searchString = charSequence.toString()
                lastSearch = searchString
                val filteredResults: List<Country> = if (searchString.isEmpty()) {
                    countriesList.toMutableList()
                } else {
                    val filteredList = countriesList.filter { country ->
                        country.dialCode.contains(charSequence, true)
                                || country.countryName.contains(charSequence, true)
                    }.sortedBy {
                        if (charSequence.startsWith('+')) {
                            it.dialCode
                        } else {
                            it.countryName
                        }
                    }

                    filteredList.toMutableList()
                }

                val filterResults = FilterResults()
                filterResults.values = filteredResults
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                countriesFiltered = filterResults.values as List<*>
                notifyDataSetChanged()
            }
        }
    }

    class CountryCodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val countryFlag: TextView = view.findViewById(R.id.countryCodeFlag)
        val countryName: TextView = view.findViewById(R.id.countryCodeName)
        val countryCode: TextView = view.findViewById(R.id.countryCodeNumber)
    }
}