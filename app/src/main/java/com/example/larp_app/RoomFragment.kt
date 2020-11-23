package com.example.larp_app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.ListFragment
import kotlinx.android.synthetic.main.fragment_room.*


class RoomFragment(arr: Array<String>) : ListFragment() {
    private val array: Array<String> = arr

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter: ArrayAdapter<String> = ArrayAdapter(activity?.baseContext!!, R.layout.item_layout, array)

        val listView = view.findViewById<View>(android.R.id.list) as ListView
        //adapter makes buttons of array
        listView.adapter = adapter
        //listener for adapter elements
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val item = list.getItemAtPosition(position)
                (activity as MainActivity).joinJoinedRoom(item.toString())
            }

        val roomName = view.findViewById<EditText>(R.id.roomName)
        val password = view.findViewById<EditText>(R.id.roomPassword)
        val create = view.findViewById<Button>(R.id.roomCreate)
        val join = view.findViewById<Button>(R.id.roomJoin)
        //create.isEnabled = false
        //join.isEnabled = false

        create.setOnClickListener {
            (activity as MainActivity).createRoom(roomName.text.toString(), password.text.toString())
        }
        join.setOnClickListener {
            (activity as MainActivity).joinRoom(roomName.text.toString(), password.text.toString())
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_room, container, false)
    }
}