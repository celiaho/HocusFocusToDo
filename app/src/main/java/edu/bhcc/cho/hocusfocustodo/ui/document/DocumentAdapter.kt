package edu.bhcc.cho.hocusfocustodo.ui.document

import android.util.Log
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.bhcc.cho.hocusfocustodo.R
import edu.bhcc.cho.hocusfocustodo.data.model.Document
import edu.bhcc.cho.hocusfocustodo.data.network.DocumentApiService
import edu.bhcc.cho.hocusfocustodo.utils.SessionManager

/**
 * Displays a list of Document items in a RecyclerView using item_document_list_tab.xml.
 */
class DocumentAdapter(
    private val context: Context,
    private var documents: List<Document>,
    // Lambda to launch DocumentActivity and receive RESULT_OK (used to trigger My Files refresh after edits)
    private val startForResult: (Intent) -> Unit,
    private val userProfiles: List<DocumentApiService.UserProfile>
) : RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder>() {
    /**
     * ViewHolder represents one item view in the RecyclerView.
     */
    inner class DocumentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.text_document_title)
        val ownerTextView: TextView = view.findViewById(R.id.text_document_owner)
        val lastModifiedText: TextView = view.findViewById(R.id.text_document_last_modified_date)
    }

    /**
     * Inflates the layout for each document item in the RecyclerView.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new View.
     * @return A new instance of DocumentViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_document_list_tab, parent, false)
        return DocumentViewHolder(view)
    }

    /**
     * Binds document data to each item view in the RecyclerView to override dummy text for Document
     * Title, Document Owner, and Last Modified Date.
     * @param holder The DocumentViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val doc = documents[holder.adapterPosition]

        holder.titleText.text = doc.title
        holder.lastModifiedText.text = "Last modified: ${doc.lastModifiedDate}"

        val currentUserId = SessionManager(context).getUserId()
        val ownerUser = userProfiles.find { it.id == doc.ownerId }
        val ownerLabel = if (doc.ownerId == currentUserId) "${ownerUser?.firstName} ${ownerUser?.lastName} (You)" else
            "${ownerUser?.firstName} ${ownerUser?.lastName}"
        holder.ownerTextView.text = "Owner: $ownerLabel"
        Log.d("---DOC_ADAPTER_OWNER_LOOKUP", "DOC_ADAPTER ownerId=${doc.ownerId}, " +
                "matchedUser=${ownerUser?.firstName} ${ownerUser?.lastName}")

        holder.itemView.setOnClickListener {
            Log.d("---DOC_ADAPTER_LAUNCHING_DOC", "---DOCUMENT ADAPTER launching DocumentActivity with DOCUMENT_ID = ${doc.id}")
            val intent = Intent(context, DocumentActivity::class.java).apply {
                putExtra("DOCUMENT_ID", doc.id)
            }
            startForResult(intent)
        }
    }

    /**
     * Returns the total number of documents to display.
     *
     * @return The size of the documents list.
     */
    override fun getItemCount(): Int = documents.size

    /**
     * Replaces the current list of documents with a new list and refreshes the RecyclerView.
     *
     * @param newDocuments The updated list of Document objects to display.
     */
    fun updateData(newDocuments: List<Document>) {
        documents = newDocuments
        notifyDataSetChanged()
    }
}