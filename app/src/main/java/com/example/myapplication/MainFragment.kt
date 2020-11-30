package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

class MainFragment : Fragment() {
    private val viewModel: MainFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.button_send).setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToFirstFragment())
        }

        view.findViewById<Button>(R.id.button_receive).setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToSecondFragment())
        }

    }

}