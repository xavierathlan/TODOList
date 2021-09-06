package com.edson.todolist.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.edson.todolist.R
import com.edson.todolist.models.Task
import com.edson.todolist.repositories.TaskRepository
import com.edson.todolist.views.CreateTaskViewModel
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private lateinit var mEditTitle: EditText
    private lateinit var mEditDesc: EditText

    private lateinit var mColorButtons: MutableList<MaterialButton>
    private lateinit var mIconButtons: MutableList<MaterialButton>

    private lateinit var mIconPicker: ImageView

    private lateinit var mEditDatePicker: TextView
    private lateinit var mEditTimePicker: TextView

    private lateinit var mButtonConfirm: Button
    private lateinit var mButtonCancel: Button

    private lateinit var mIconDialog: Dialog

    private var mPosition: Int? = null

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
        setContentView(R.layout.activity_create_task)

        mPosition = intent.getIntExtra("position", 0)

        initializeUI()
        configureObservers()
        configureListeners()
    }

    private fun configureListeners() {
        for (i in 0..4)
        {
            mColorButtons[i].setOnClickListener { mViewModel.setColor(i) }
        }

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
        mButtonConfirm.setOnClickListener {
            var hasError = false

            if (mEditTitle.text.trim().isEmpty()) {
                mEditTitle.error = "O campo de titulo não poder ficar vazio"
                hasError = true;
            }

            if (mEditDesc.text.trim().isEmpty()) {
                mEditDesc.setError("O campo de descrição não poder ficar vazio")
                hasError = true;
            }

            if (!hasError) {
                val task = Task(null, mEditTitle.text.trim().toString(), mEditDesc.text.trim().toString(), mPosition!!)
                task.done = false
                task.inCalendar = false
                task.icon = mViewModel.getIcon()
                task.color = mViewModel.getColor()
                if (mViewModel.getDate() != null) {
                    task.date = mViewModel.getDate()
                }
                val repository = TaskRepository()
                repository.create(task, null)
                setResult(0)
                finish()
            }
        }

        // Evento do botao cancelar
        mButtonCancel.setOnClickListener {
            setResult(-1)
            finish()
        }
    }

    private fun configureObservers() {
        var lastSelectedColor: Int? = null

        mViewModel.observeDate {
            updateDateField(it, mViewModel.getChangeDateFlag()!!)
            mViewModel.setChangeDateFlag(0)
        }

        mViewModel.observeColor {
            if (lastSelectedColor != null)
            {
                unselectColor(lastSelectedColor!!)
            }
            selectColor(it)
            lastSelectedColor = it
        }

        mViewModel.observeIcon {
            mIconPicker.setImageResource(mIconList[it])
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDateField(date: Date, changeFlag: Int) {
        when (changeFlag)
        {
            1 -> {
                val calendar = Calendar.getInstance()
                calendar.time = date

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DATE)

                val strMonth = (month + 1).toString().padStart(2, '0')
                val strDay = (day).toString().padStart(2, '0')

                mEditDatePicker.setText("$strDay/${strMonth}/$year")
            }

            2 -> {
                val calendar = Calendar.getInstance()
                calendar.time = date

                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minutes = calendar.get(Calendar.MINUTE)

                val strHour = hour.toString().padStart(2, '0')
                val strMinutes = minutes.toString().padStart(2, '0')

                mEditTimePicker.setText("${strHour}:${strMinutes}")
            }

            else -> {}
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

        mIconDialog = Dialog(this)

        mIconDialog.setContentView(R.layout.icon_picker_dialog)

        mButtonConfirm = findViewById(R.id.confirm)
        mButtonCancel = findViewById(R.id.cancel)

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