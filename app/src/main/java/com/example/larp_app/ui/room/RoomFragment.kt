package com.example.larp_app.ui.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.ListFragment
import com.example.larp_app.MainActivity
import com.example.larp_app.R
import kotlinx.android.synthetic.main.fragment_room.*
import org.json.JSONArray


class RoomFragment(arr: String) : ListFragment() {
    private var array: String = arr

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = ArrayList<String>()
        val json = JSONArray(array)
        for (i in 0 until json.length()) {
            data.add(json.getString(i))
        }
        val adapter: ArrayAdapter<String> = ArrayAdapter(
            activity?.baseContext!!,
            R.layout.item_layout, data
        )

        val listView = view.findViewById<View>(android.R.id.list) as ListView
        //adapter makes buttons of array
        listView.adapter = adapter
        //listener for adapter elements
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val item = list.getItemAtPosition(position)
                (activity as MainActivity).joinJoinedRoom(item.toString())
            }

        val roomName = view.findViewById<EditText>(R.id.roomName)
        val password = view.findViewById<EditText>(R.id.roomPassword)
        val create = view.findViewById<Button>(R.id.roomCreate)
        val join = view.findViewById<Button>(R.id.roomJoin)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)

        //create.isEnabled = false
        //join.isEnabled = false

        create.setOnClickListener {
            val radioButton =
                radioGroup.findViewById<View>(radioGroup.checkedRadioButtonId) as RadioButton
            val team = radioButton.text.toString().toInt()
            (activity as MainActivity).createRoom(
                roomName.text.toString(),
                password.text.toString(),
                team
            )
        }
        join.setOnClickListener {
            val radioButton =
                radioGroup.findViewById<View>(radioGroup.checkedRadioButtonId) as RadioButton
            val team = radioButton.text.toString().toInt()
            (activity as MainActivity).joinRoom(
                roomName.text.toString(),
                password.text.toString(),
                team
            )
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