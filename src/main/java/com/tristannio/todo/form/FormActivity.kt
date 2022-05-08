package com.tristannio.todo.form

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.tristannio.todo.R
import com.tristannio.todo.databinding.ActivityFormBinding
import com.tristannio.todo.tasklist.Task
import java.util.*

class FormActivity : AppCompatActivity() {

    private var _binding: ActivityFormBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        _binding = ActivityFormBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val task = intent.getSerializableExtra( "task") as? Task

        val editTextTitle = binding.title
        editTextTitle.setText(task?.title)

        val editTextDescription = binding.description
        editTextDescription.setText(task?.description)

        val id = task?.id ?: UUID.randomUUID().toString()

        findViewById<ImageButton>(R.id.imageButtonV).setOnClickListener {
            val title = editTextTitle.text.toString()
            val description = editTextDescription.text.toString()
            val newTask = Task(id = id, title = title, description = description);
            intent.putExtra("task", newTask)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}