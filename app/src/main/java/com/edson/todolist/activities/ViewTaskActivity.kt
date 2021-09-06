package com.edson.todolist.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.button.MaterialButton
import com.edson.todolist.R
import com.edson.todolist.models.Task
import com.edson.todolist.repositories.TaskRepository
import com.edson.todolist.views.CreateTaskViewModel
import java.text.SimpleDateFormat
import java.util.*

class ViewTaskActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private lateinit var mEditTitle: EditText
    private lateinit var mEditDesc: EditText

    private lateinit var mColorButtons: MutableList<MaterialButton>

    private lateinit var mIconPicker: ImageView

    private lateinit var mEditDatePicker: TextView
    private lateinit var mEditTimePicker: TextView

    private lateinit var mButtonSetDone: Button

    private lateinit var mIconDialog: Dialog

    private lateinit var mIconButtons: MutableList<MaterialButton>

    private lateinit var mStartForResult: ActivityResultLauncher<Intent?>

    private lateinit var mLoadingContainer: FrameLayout

    private var mTask: Task? = null

    private var mHasChange: Boolean = false

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

    private val mViewModel: CreateTaskViewModel = CreateTaskViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_task)

        initializeUI()
        configureObservers()

        if (intent.hasExtra("taskId")) {
            val repository = TaskRepository()
            repository.get(intent.getStringExtra("taskId")!!) {
                mTask = it

                updateTitleAndDesc(it)

                mTask!!.date?.let { mViewModel.setDate(it) }
                mTask!!.icon?.let { mViewModel.setIcon(it) }
                mTask!!.color?.let { mViewModel.setColor(it) }

                mViewModel.setDone(mTask!!.done)
                mViewModel.setInCalendar(mTask!!.inCalendar)

                configureListeners()

                mLoadingContainer.visibility = View.GONE
                mHasChange = false
            }
        }
        else
        {
            finish()
        }
    }

    private fun updateTitleAndDesc(it: Task) {
        mEditTitle.setText(it.title)
        mEditDesc.setText(it.desc)
    }

    override fun onBackPressed() {
        if (mHasChange) {
            showAskSaveDialog({
                val task = Task(mTask!!.id, mEditTitle.text.trim().toString(), mEditDesc.text.trim().toString(), mTask!!.position)
                task.done = mViewModel.getDone()!!
                task.inCalendar = mViewModel.getInCalendar()!!
                task.icon = mViewModel.getIcon()
                task.color = mViewModel.getColor()
                if (mViewModel.getDate() != null) {
                    task.date = mViewModel.getDate()
                }
                mTask = task
                val repository = TaskRepository()
                repository.update(task) {}
                setResult(0)
                finish()
            }, {
                setResult(0)
                finish()
            })
        }
        else
        {
            setResult(0)
            finish()
        }
    }

    private fun showAskSaveDialog(positive: () -> Unit, negative: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Salvar alterações")
            .setView(LayoutInflater.from(this).inflate(R.layout.ask_save_fragment, null))
            .setPositiveButton("Salvar") { dialog, witch ->
                positive()
            }
            .setNegativeButton("Não salvar") { dialog, witch ->
                negative()
            }
            .create()
            .show()
    }

    private fun configureListeners() {
        for (i in 0..4)
        {
            mColorButtons[i].setOnClickListener { mViewModel.setColor(i) }
        }

        mEditTitle.addTextChangedListener { mHasChange = true }
        mEditDesc.addTextChangedListener { mHasChange = true }

        // Abre o dialog para selecionar um icone
        mIconPicker.setOnClickListener {
            var selectedIcon: Int = mViewModel.getIcon()!!
            val confirmButton = mIconDialog.findViewById(R.id.confirm) as Button

            mIconButtons[selectedIcon].strokeWidth = 10

            for (idx in 0..12) {
                mIconButtons[idx].setOnClickListener {
                    mIconButtons[selectedIcon].strokeWidth = 0
                    mIconButtons[idx].strokeWidth = 10
                    selectedIcon = idx
                }
            }

            confirmButton.setOnClickListener {
                mViewModel.setIcon(selectedIcon)
                mIconDialog.dismiss()
            }

            mIconDialog.show()
        }

        // Abre o datepíckerdialog
        mEditDatePicker.setOnClickListener {
            val year = Calendar.getInstance().get(Calendar.YEAR)
            val month = Calendar.getInstance().get(Calendar.MONTH)
            val day = Calendar.getInstance().get(Calendar.DATE)

            val dialog = DatePickerDialog(
                this,
                this,
                year, month, day
            )

            dialog.show()
        }

        mEditTimePicker.setOnClickListener {
            if (mViewModel.getDate() == null)
            {
                Toast.makeText(this, "Primeiro você deve selecionar uma data", Toast.LENGTH_LONG).show()
            }
            else
            {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR)
                val minutes = calendar.get(Calendar.MINUTE)

                val dialog = TimePickerDialog(this, this, hour, minutes, true)

                dialog.show()
            }
        }

        // Evento do botao confirmar
        mButtonSetDone.setOnClickListener {
            var hasError = false

            if (mEditTitle.text.trim().isEmpty()) {
                mEditTitle.error = "O campo de titulo não poder ficar vazio"
                hasError = true;
            }

            if (mEditDesc.text.trim().isEmpty()) {
                mEditDesc.setError("O campo de descrição não poder ficar vazio")
                hasError = true;
            }

            if (mViewModel.getHour() != null && mViewModel.getMinutes() != null) {
                if (mViewModel.getDate() == null) {
                    Toast.makeText(this, "Você deve selecionar uma data", Toast.LENGTH_LONG)
                        .show()
                    hasError = true;
                }
            }

            if (!hasError) {
                if (mHasChange)
                {
                    showAskSaveDialog({
                        val task = Task(mTask!!.id, mEditTitle.text.trim().toString(), mEditDesc.text.trim().toString(), mTask!!.position)
                        task.inCalendar = mViewModel.getInCalendar()!!
                        task.icon = mViewModel.getIcon()
                        task.color = mViewModel.getColor()
                        if (mViewModel.getDate() != null) {
                            task.date = mViewModel.getDate()
                        }
                        mTask = task

                        when (mViewModel.getDone()) {
                            true -> { mTask!!.done = false }
                            false -> { mTask!!.done = true }
                            else -> {}
                        }
                        val repository = TaskRepository()
                        repository.update(task) {
                            mViewModel.setDone(it.done)
                        }

                        mHasChange = false
                    }, {
                        when (mViewModel.getDone()) {
                            true -> { mTask!!.done = false }
                            false -> { mTask!!.done = true }
                            else -> {}
                        }

                        val repository = TaskRepository()
                        repository.update(mTask!!) {
                            mViewModel.setDone(it.done)
                        }

                        mHasChange = false
                    })
                }
                else
                {
                    when (mViewModel.getDone()) {
                        true -> { mTask!!.done = false }
                        false -> { mTask!!.done = true }
                        else -> {}
                    }

                    val repository = TaskRepository()
                    repository.update(mTask!!) {
                        mViewModel.setDone(it.done)
                    }

                    mHasChange = false
                }
            }
        }
    }

    private fun configureObservers() {
        var lastSelectedColor: Int? = null

        mViewModel.observeDate {
            updateDateField(it)
            mHasChange = true
        }

        mViewModel.observeColor {
            if (lastSelectedColor != null)
            {
                unselectColor(lastSelectedColor!!)
            }
            selectColor(it)
            lastSelectedColor = it
            mHasChange = true
        }

        mViewModel.observeIcon {
            mIconPicker.setImageResource(mIconList[it])
            mHasChange = true
        }

        mViewModel.observeDone {
            when (it) {
                true -> {
                    mButtonSetDone.setText(resources.getText(R.string.set_undone))
                }
                false -> {
                    mButtonSetDone.setText(resources.getText(R.string.set_done))
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDateField(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DATE)

        val strMonth = (month + 1).toString().padStart(2, '0')
        val strDay = (day).toString().padStart(2, '0')

        mEditDatePicker.setText("$strDay/${strMonth}/$year")

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)

        if (hour != 0 && minutes != 0) {
            val strHour = hour.toString().padStart(2, '0')
            val strMinutes = minutes.toString().padStart(2, '0')

            mEditTimePicker.setText("${strHour}:${strMinutes}")
        }
    }

    private fun initializeUI() {
        mEditTitle = findViewById(R.id.editTitle)
        mEditDesc = findViewById(R.id.editDescription)

        mColorButtons = mutableListOf(
            findViewById(R.id.colorOne),
            findViewById(R.id.colorTwo),
            findViewById(R.id.colorThree),
            findViewById(R.id.colorFour),
            findViewById(R.id.colorFive)
        )

        mIconPicker = findViewById(R.id.iconPicker)

        mEditDatePicker = findViewById(R.id.editDatePicker)
        mEditTimePicker = findViewById(R.id.editTimePicker)

        mButtonSetDone = findViewById(R.id.confirm)

        mIconDialog = Dialog(this)

        mIconDialog.setContentView(R.layout.icon_picker_dialog)

        mIconButtons = arrayListOf(
            mIconDialog.findViewById(R.id.buttonOne),
            mIconDialog.findViewById(R.id.buttonTwo),
            mIconDialog.findViewById(R.id.buttonThree),
            mIconDialog.findViewById(R.id.buttonFour),
            mIconDialog.findViewById(R.id.buttonFive),
            mIconDialog.findViewById(R.id.buttonSix),
            mIconDialog.findViewById(R.id.buttonSeven),
            mIconDialog.findViewById(R.id.buttonEight),
            mIconDialog.findViewById(R.id.buttonNine),
            mIconDialog.findViewById(R.id.buttonTen),
            mIconDialog.findViewById(R.id.buttonEleven),
            mIconDialog.findViewById(R.id.buttonTwelve),
            mIconDialog.findViewById(R.id.buttonThirteen)
        )

        mLoadingContainer = findViewById(R.id.loadingContainer)

        mStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == 0)
            {
                mViewModel.setInCalendar(true)
            }
        }
    }

    private fun unselectColor(idx: Int)
    {
        mColorButtons[idx].strokeWidth = 0
    }

    private fun selectColor(idx: Int)
    {
        mColorButtons[idx].strokeWidth = 8
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("pt", "BR"))
        mViewModel.setDate(simpleDateFormat.parse("$year-${month + 1}-$dayOfMonth")!!)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        mViewModel.setTime(hourOfDay, minute)
    }
}