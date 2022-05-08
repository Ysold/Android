package com.tristannio.todo.tasklist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tristannio.todo.R
import com.tristannio.todo.databinding.FragmentTaskListBinding
import com.tristannio.todo.form.FormActivity
import com.tristannio.todo.network.Api
import com.tristannio.todo.user.UserInfoActivity
import kotlinx.coroutines.launch


class TaskListFragment : Fragment() {

    private var taskList = listOf(
        Task(id = "id_1", title = "Task 1", description = "description 1"),
        Task(id = "id_2", title = "Task 2"),
        Task(id = "id_3", title = "Task 3")
    )

    val createTask = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = result.data?.getSerializableExtra("task") as? Task
            ?: return@registerForActivityResult
        lifecycleScope.launch {
            viewModel.create(task);
        }
        adapter.submitList(taskList)
    }

    val editTask = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = result.data?.getSerializableExtra("task") as? Task
            ?: return@registerForActivityResult
        lifecycleScope.launch {
            viewModel.update(task);
        }
        adapter.submitList(taskList)
    }


    private val adapter = TaskListAdapter()

    private val viewModel: TasksListViewModel by viewModels()

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.submitList(taskList)
        val recyclerView = view.findViewById<RecyclerView>(R.id.fragment_main_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        adapter.onClickDelete = { task ->
            lifecycleScope.launch {
                viewModel.delete(task);
            }
            adapter.submitList(taskList)
        }

        adapter.onClickEdit = { task ->
            val intent = Intent(context, FormActivity::class.java)
            intent.putExtra("task", task)
            editTask.launch(intent)

        }

        view.findViewById<FloatingActionButton>(R.id.floatingActionButton2).setOnClickListener {
            val intent = Intent(context, FormActivity::class.java)
            createTask.launch(intent)
        }

        val avatarImageView = binding.avatar

        avatarImageView.setOnClickListener{
            val intent = Intent(context, UserInfoActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            viewModel.tasksStateFlow.collect { newList ->
                taskList = newList
                adapter.submitList(taskList)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val avatarImageView = binding.avatar

        avatarImageView.load("https://goo.gl/gEgYUd"){
            crossfade(true)
            transformations(CircleCropTransformation())
        }

        lifecycleScope.launch {
            viewModel.refresh()
        }
    }
}

