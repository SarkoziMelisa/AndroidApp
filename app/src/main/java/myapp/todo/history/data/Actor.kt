package myapp.todo.history.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actors")
data class Actor(
    @PrimaryKey @ColumnInfo(name = "_id") val _id: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "wikipediaLink") var wikipediaLink: String,
    @ColumnInfo(name = "matchingPercentage") var matchingPercentage: Float
) {
    override fun toString(): String = "$name $wikipediaLink $matchingPercentage\n"
}