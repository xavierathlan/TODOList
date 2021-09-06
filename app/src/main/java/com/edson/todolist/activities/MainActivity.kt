package com.edson.todolist.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.edson.todolist.R
import com.edson.todolist.adapters.TaskAdapter
import com.edson.todolist.models.Task
import com.edson.todolist.repositories.TaskRepository
import com.edson.todolist.views.MainViewModel
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var mAddTaskButton: FloatingActionButton
    private lateinit var mViewModel: MainViewModel
    private lateinit var mSignUILauncher: ActivityResultLauncher<Intent>
    private lateinit var mTaskRecycler: RecyclerView
    private lateinit var mAdapter: TaskAdapter
    private lateinit var mSplash: FrameLayout
    private lateinit var mUserName: TextView
    private lateinit var mUserImage: ImageView
    private val mRepository: TaskRepository = TaskRepository()
    private lateinit var mStartForResult: ActivityResultLauncher<Intent?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTaskRecycler = findViewById(R.id.taskRecycler)
        mSplash = findViewById(R.id.splashContainer)
        mUserName = findViewById(R.id.userName)
        mUserImage = findViewById(R.id.userImage)

        mStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == 0)
            {
                updateTask(false)
            }
        }

        // Cria o view model
        mViewModel = MainViewModel()

        mViewModel.observeUser {
            mUserName.setText(it.displayName)

            it.photoUrl?.let {
                Picasso
                    .with(this)
                    .load(it)
                    .into(mUserImage)
            }
        }

        // Inicia activity de autenticacao
        launchSignInActivity()

        // Abre a janela para adicionar uma nova tarefa.
        mAddTaskButton = findViewById(R.id.addTask) as FloatingActionButton
        mAddTaskButton.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            intent.putExtra("position", mAdapter.itemCount)
            mStartForResult.launch(intent)
        }

        val viewTask: (Task) -> Unit = {
            val intent = Intent(this, ViewTaskActivity::class.java)
            intent.putExtra("taskId", it.id)
            mStartForResult.launch(intent)
        }

        val deleteTask: (Task) -> Unit = {
            AlertDialog.Builder(this)
                .setTitle("Deletar tarefa?")
                .setView(LayoutInflater.from(this).inflate(R.layout.ask_delete_fragment, null))
                .setPositiveButton("Apagar") { _, _ ->
                    mRepository.delete(it) {
                        updateTask(true)
                    }
                }
                .setNegativeButton("Não apagar") { _, _ -> }
                .create()
                .show()
        }

        val markTask: (Intent) -> Unit = {
            startActivity(it)
        }
        mAdapter = TaskAdapter(this, mRepository, viewTask, deleteTask, markTask)

        // Atualiza registros
        updateTask(false)

        mTaskRecycler.adapter = mAdapter
        mTaskRecycler.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper = ItemTouchHelper(mItemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(mTaskRecycler)
    }

    private fun launchSignInActivity() {
        mSignUILauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { res ->
            this.onSignInResult(res)
        }

        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInItent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        mSignUILauncher.launch(signInItent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            FirebaseAuth.getInstance().currentUser?.let{ mViewModel.setUser(it) }

            // Remove splash
            mSplash.visibility = View.GONE
        } else {
            finish()
        }
    }

    private fun updateTask(needRearrange: Boolean)
    {
        mRepository.getAll {
            val mutableTaskList = mutableListOf<Task>()
            mutableTaskList.addAll(it)

            val pendingTasks: ArrayList<Task> = arrayListOf()
            val completeTasks: ArrayList<Task> = arrayListOf()

            pendingTasks.addAll(mutableTaskList.filter { it.done == false })
            completeTasks.addAll(mutableTaskList.filter { it.done == true })

            mutableTaskList.clear()
            pendingTasks.sortBy { it.position }
            mutableTaskList += pendingTasks
            completeTasks.sortBy { it.position }
            mutableTaskList += completeTasks

            mAdapter.updateTaskList(mutableTaskList as ArrayList<Task>)

            if (needRearrange) {
                mAdapter.rearrangeTaskPosition()
            }
        }
    }

    val mItemTouchHelperCallback = object:ItemTouchHelper.Callback() {
        private var fromDrag: Int = -1
        private var toDrag: Int = -1

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return onMoveTaskHolder(
                recyclerView,
                viewHolder as TaskAdapter.TaskViewHolder,
                target as TaskAdapter.TaskViewHolder
            )
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)

            if (fromDrag != -1 && toDrag != -1 && fromDrag != toDrag)
            {
                mAdapter.rearrangeTaskPosition()

                fromDrag = -1
                toDrag = -1
            }
        }

        private fun onMoveTaskHolder(
            recyclerView: RecyclerView,
            viewHolder: TaskAdapter.TaskViewHolder,
            target: TaskAdapter.TaskViewHolder): Boolean
        {
            if (viewHolder.getTask().done != target.getTask().done) {
                return false;
            }

            if (fromDrag == -1) {
                fromDrag = viewHolder.adapterPosition
            }
            toDrag = target.adapterPosition

            mAdapter.swap(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }
    }
}