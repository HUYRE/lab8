package com.example.lab8

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface TaskDao {


    // Obtener todas las tareas
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>


    // Insertar una nueva tarea
    @Insert
    suspend fun insertTask(task: Task)


    // Marcar una tarea como completada o no completada
    @Update
    suspend fun updateTask(task: Task)




    // Eliminar todas las tareas
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    fun deleteTask(task: Task) {}
    @Query("SELECT * FROM tasks WHERE is_completed = 1")
    suspend fun getCompletedTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE is_completed = 0")
    suspend fun getPendingTasks(): List<Task>

}
