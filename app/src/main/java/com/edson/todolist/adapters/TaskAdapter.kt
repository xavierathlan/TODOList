package com.edson.todolist.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.edson.todolist.R
import com.edson.todolist.models.Task
import com.edson.todolist.repositories.TaskRepository
import java.util.*
import kotlin.collections.ArrayList

class TaskAdapter(private val context: Context, private val repository: TaskRepository, private val view: (Task) -> Unit, private val delete: (Task) -> Unit, private val mark: (Intent) -> Unit) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    private var mTaskList = arrayListOf<Task>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder =
        TaskViewHolder(LayoutInflater.from(context).inflate(R.layout.fragment_task, parent, false), view, delete, mark)

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = mTaskList[position];
        holder.bind(task)
    }

    override fun getItemCount(): Int =
        mTaskList.size

    fun updateTaskList(list: ArrayList<Task>)
    {
        mTaskList = list
        notifyDataSetChanged()
    }

    fun swap(fromPosition: Int, toPosition: Int) {
        Collections.swap(mTaskList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun rearrangeTaskPosition()
    {
        for (i in 0..mTaskList.size-1)
        {
            mTaskList[i].position = i
            repository.update(mTaskList[i]) {}
        }
    }

    class TaskViewHolder(itemView: View, private val view: (Task) -> Unit, private val delete: (Task) -> Unit, private val mark: (Intent) -> Unit) : RecyclerView.ViewHolder(itemView)
    {
        private val mTitle: TextView = itemView.findViewById(R.id.title)
        private val mDesc: TextView = itemView.findViewById(R.id.description)
        private val mDate: TextView = itemView.findViewById(R.id.date)
        private val mTime: TextView = itemView.findViewById(R.id.time)
        private val mColor: View = itemView.findViewById(R.id.color)
        private val mIcon: ImageView = itemView.findViewById(R.id.icon)
        private val mDone: ImageView = itemView.findViewById(R.id.done)
        private val mDelete: ImageView = itemView.findViewById(R.id.deleteButton)
        private val mEdit: ImageView = itemView.findViewById(R.id.editButton)
        private val mMark: ImageView = itemView.findViewById(R.id.markInCalendarButton)
        private lateinit var mTask: Task
        private val mIconList = arrayListOf(
            R.drawable.ic_alarm,
            R.drawable.ic_balance,
            R.drawable.ic_box,
            R.drawable.ic_tree,
            R.drawable.ic_air,
            R.drawable.ic_units,
            R.drawable.ic_suite,
            R.drawable.ic_airplay,
            R.drawable.ic_inclusive,
            R.drawable.ic_anchor,
            R.drawable.ic_architecture,
            R.drawable.ic_downward,
            R.drawable.ic_photo
        )

        @SuppressLint("SetTextI18n")
        fun bind(task: Task)
        {
            mTask = task;

            mTitle.setText(task.title)
            mDesc.setText(task.desc)

            if (task.date == null)
            {
                mDate.visibility = View.GONE
                mTime.visibility = View.GONE
                mMark.visibility = View.GONE
            }
            else
            {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("america/sao_paulo"))
                calendar.time = task.date

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minutes = calendar.get(Calendar.MINUTE)

                val strMonth = month.toString().padStart(2, '0')
                val strDay = dayOfMonth.toString().padStart(2, '0')

                val strHour = hour.toString().padStart(2, '0')
                val strMinutes = minutes.toString().padStart(2, '0')

                mDate.visibility = View.VISIBLE
                if (hour == 0 && minutes == 0)
                {
                    mDate.setText("$strDay/$strMonth/$year")
                    mTime.visibility = View.INVISIBLE
                }
                else
                {
                    mTime.visibility = View.VISIBLE
                    mDate.setText("$strDay/$strMonth/$year")
                    mTime.setText("${strHour}:${strMinutes}")
                }
            }

            if (task.color == null)
            {
                mColor.visibility = View.INVISIBLE
            }
            else
            {
                mColor.visibility = View.VISIBLE
                when (task.color)
                {
                    0 -> { mColor.background.setTint(RED) }
                    1 -> { mColor.background.setTint(ORANGE) }
                    2 -> { mColor.background.setTint(YELLOW) }
                    3 -> { mColor.background.setTint(LIGHT_GREEN) }
                    4 -> { mColor.background.setTint(GREEN) }
                    else -> {}
                }
            }

            if (task.icon == null)
            {
                mIcon.visibility = View.INVISIBLE
            }
            else
            {
                mIcon.visibility = View.VISIBLE
                mIcon.setImageResource(mIconList[task.icon!!])
            }

            if (task.done)
            {
                mDone.visibility = View.VISIBLE
                itemView.background.setTint(DONE_BG)
                mMark.visibility = View.GONE
            }
            else
            {
                mDone.visibility = View.INVISIBLE
                itemView.background.setTint(Color.WHITE)
            }

            // Atribui callback para deletar a tarefa
            mDelete.setOnClickListener {
                delete(task)
            }

            // Atribui callback para visualizar/editar a tarefa
            mEdit.setOnClickListener {
                view(task)
            }

            // Atribui evento para marcar evento no calendario
            mMark.setOnClickListener {
                if (!task.done && task.date != null)
                {
                    val color = when (task.color)
                    {
                        0 -> RED
                        1 -> ORANGE
                        2 -> YELLOW
                        3 -> LIGHT_GREEN
                        4 -> GREEN
                        else -> RED
                    }

                    val intent = Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.Events.TITLE, task.title)
                        .putExtra(CalendarContract.Events.DESCRIPTION, task.desc)
                        .putExtra(CalendarContract.Events.EVENT_COLOR, color)

                    if (task.date != null) {
                        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, task.date)
                    }

                    mark(intent)
                }
            }
        }

        fun getTask() = mTask

        companion object {
            @ColorInt val RED: Int = -635571
            @ColorInt val ORANGE: Int = -31421
            @ColorInt val YELLOW: Int = -5317
            @ColorInt val LIGHT_GREEN: Int = -4852196
            @ColorInt val GREEN: Int = -15026916
            @ColorInt val DONE_BG: Int = -199957
        }
    }
}