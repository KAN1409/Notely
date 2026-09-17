package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Converters
import com.example.data.model.FollowUpEntity
import com.example.data.model.NoteEntity
import com.example.data.model.RelationshipEntity
import com.example.data.model.ReminderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        NoteEntity::class,
        RelationshipEntity::class,
        FollowUpEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun followUpDao(): FollowUpDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notely_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial realistic data
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { seedInitialData(it) }
                        }
                    }
                })
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(db: AppDatabase) {
            try {
                val now = System.currentTimeMillis()
            val oneDay = 86400000L

            val note1 = NoteEntity(
                id = 1,
                sourceType = "VOICE",
                createdAt = now - (2 * 3600 * 1000), // 2 hours ago (Today 10:24 AM)
                updatedAt = now - (2 * 3600 * 1000),
                originalInput = "هنا فكرة بخصوص إضاءة الريسبشن ونخلّيها أدفى ونستخدم إضاءة مخفية في السقف وننسق مع أحمد بخصوص عينات الرخام ونراجع مقاسات الموقع يوم الاتنين.",
                mediaPath = null,
                mediaDurationMs = 134000L, // 2:14
                rawTranscript = "هنا فكرة بخصوص إضاءة الريسبشن ونخلّيها أدفى ونستخدم إضاءة مخفية في السقف وننسق مع أحمد بخصوص عينات الرخام ونراجع مقاسات الموقع يوم الاتنين.",
                cleanedTranscript = "Here's an idea about the reception lighting: make it warmer, use hidden cove lighting in the ceiling, coordinate with Ahmed regarding marble samples, and review site measurements on Monday.",
                title = "Site visit thoughts",
                summary = "You visited the site and discussed reception area lighting. Main points include warmer lighting, marble options, and timeline for execution.",
                keyPoints = listOf(
                    "Warmer hidden cove lighting recommended for reception ceiling",
                    "Coordinate with Ahmed for Galala marble sample approvals",
                    "Conduct final site measurement on Monday at 10:00 AM"
                ),
                noteType = "MEETING",
                intent = "Capture site visit decisions and action items",
                topics = listOf("Reception", "Lighting", "Marble", "Site", "Timeline"),
                tags = listOf("Design", "Site Visit", "Negma"),
                people = listOf("Ahmed"),
                projects = listOf("Negma Project"),
                places = listOf("Reception Area", "Site"),
                monetaryValues = emptyList(),
                importance = "HIGH",
                actionability = "FOLLOW_UP",
                suggestedActions = listOf(
                    "Create follow-up with Ahmed for marble quotation",
                    "Add reminder for site measurement on Monday"
                )
            )

            val note2 = NoteEntity(
                id = 2,
                sourceType = "IMAGE",
                createdAt = now - oneDay,
                updatedAt = now - oneDay,
                originalInput = "Hilton Cairo Quotation - Marble Galala 2450 EGP/m2 valid until 30/09/2026 supplier Ahmed Co.",
                ocrText = "QUOTATION #QT-8821\nSupplier: Ahmed Marble & Granite Co.\nProject: Negma Project - Reception\nItem: Galala Cream Marble (Polished)\nUnit Price: 2,450 EGP / m²\nEstimated Quantity: 180 m²\nTotal Estimate: 441,000 EGP\nValidity: 30 September 2026\nPayment terms: 50% advance, 50% on delivery",
                title = "Hotel quotation - Marble",
                summary = "Official quotation from Ahmed Co. for Galala Cream marble at 2,450 EGP/m² for reception flooring.",
                keyPoints = listOf(
                    "Unit price 2,450 EGP per square meter for Galala marble",
                    "Quote valid until September 30, 2026",
                    "Payment terms: 50% advance required before fabrication"
                ),
                noteType = "PURCHASE",
                intent = "Document vendor quotation for procurement",
                topics = listOf("Quotation", "Marble", "Procurement"),
                tags = listOf("Finance", "Vendor", "Quotation"),
                people = listOf("Ahmed"),
                projects = listOf("Negma Project"),
                places = listOf("Cairo"),
                monetaryValues = listOf("2,450 EGP / m²", "441,000 EGP"),
                importance = "HIGH",
                actionability = "FOLLOW_UP",
                suggestedActions = listOf("Follow up with Ahmed on discounted price for bulk order")
            )

            val note3 = NoteEntity(
                id = 3,
                sourceType = "URL",
                createdAt = now - (oneDay + 3600000),
                updatedAt = now - (oneDay + 3600000),
                originalInput = "https://architecturaldigest.com/story/japandi-interior-design-balance",
                webUrl = "https://architecturaldigest.com/story/japandi-interior-design-balance",
                webTitle = "Japandi Interior Design The Perfect Balance",
                webContent = "Japandi design combines Scandinavian functionality with Japanese rustic minimalism to create a feeling of art, nature, and simplicity. Focus on clean lines, warm woods, neutral tones, and generous natural lighting.",
                title = "Design inspiration - Japandi",
                summary = "Article exploring Japandi interior aesthetics: combining Scandinavian warm minimalism with Japanese organic materials and indirect lighting.",
                keyPoints = listOf(
                    "Neutral earthy palettes with black subtle metal accents",
                    "Emphasis on natural wood finishes and acoustic wood slats",
                    "Hidden warm light temperature (2700K to 3000K)"
                ),
                noteType = "REFERENCE",
                intent = "Keep design reference for reception mood board",
                topics = listOf("Design", "Japandi", "Minimalism", "Lighting"),
                tags = listOf("Inspiration", "Architecture"),
                people = emptyList(),
                projects = listOf("Negma Project"),
                places = emptyList(),
                monetaryValues = emptyList(),
                importance = "NORMAL",
                actionability = "NONE",
                suggestedActions = listOf("Share mood board with client")
            )

            val note4 = NoteEntity(
                id = 4,
                sourceType = "TEXT",
                createdAt = now - (3 * oneDay),
                updatedAt = now - (3 * oneDay),
                originalInput = "Meeting with Yasser regarding design approval for Medical Center PR 0262. Need final signoff by Thursday.",
                title = "Yasser approval - Medical Center",
                summary = "Discussed pending signoff with Yasser regarding Medical Center PR 0262 design packet.",
                keyPoints = listOf(
                    "Awaiting Yasser's signature on MEP revisions",
                    "Final signoff due by Thursday end of day"
                ),
                noteType = "FOLLOW_UP",
                intent = "Track open loop waiting for signoff",
                topics = listOf("Approval", "Medical Center", "MEP"),
                tags = listOf("Approvals", "Medical"),
                people = listOf("Yasser"),
                projects = listOf("Medical Center"),
                places = listOf("Cairo"),
                monetaryValues = emptyList(),
                importance = "HIGH",
                actionability = "FOLLOW_UP",
                suggestedActions = listOf("Follow up with Yasser regarding design approval")
            )

            val note5 = NoteEntity(
                id = 5,
                sourceType = "TEXT",
                createdAt = now - (4 * oneDay),
                updatedAt = now - (4 * oneDay),
                originalInput = "Doctor appointment with Dr. Tarek for Rheumatology checkup at El Safa Hospital on Wednesday 1:00 PM.",
                title = "Doctor appointment - Rheumatology",
                summary = "Scheduled checkup with Dr. Tarek at El Safa Hospital on Wednesday at 1:00 PM.",
                keyPoints = listOf(
                    "Location: El Safa Hospital clinic #4",
                    "Bring previous blood analysis and vitamin D report"
                ),
                noteType = "REMINDER",
                intent = "Schedule personal medical appointment",
                topics = listOf("Health", "Appointment"),
                tags = listOf("Personal", "Health"),
                people = listOf("Dr. Tarek"),
                projects = emptyList(),
                places = listOf("El Safa Hospital"),
                monetaryValues = emptyList(),
                importance = "URGENT",
                actionability = "REMINDER",
                suggestedActions = listOf("Set calendar alert 1 hour prior")
            )

            db.noteDao().insertNote(note1)
            db.noteDao().insertNote(note2)
            db.noteDao().insertNote(note3)
            db.noteDao().insertNote(note4)
            db.noteDao().insertNote(note5)

            // Seed Relationships
            db.relationshipDao().insertRelationship(
                RelationshipEntity(
                    sourceNoteId = 1,
                    targetNoteId = 2,
                    relationshipType = "SAME_PROJECT",
                    explanation = "Both discuss marble procurement for Negma Project reception with Ahmed"
                )
            )
            db.relationshipDao().insertRelationship(
                RelationshipEntity(
                    sourceNoteId = 1,
                    targetNoteId = 3,
                    relationshipType = "SAME_TOPIC",
                    explanation = "Both concern warm hidden lighting and aesthetics for the reception"
                )
            )
            db.relationshipDao().insertRelationship(
                RelationshipEntity(
                    sourceNoteId = 2,
                    targetNoteId = 1,
                    relationshipType = "SAME_PERSON",
                    explanation = "Ahmed is the key vendor in both the quotation and site discussion"
                )
            )

            // Seed Follow-Ups
            db.followUpDao().insertFollowUp(
                FollowUpEntity(
                    noteId = 2,
                    subject = "Ahmed - marble quotation",
                    context = "Waiting for updated price and bulk discount confirmation",
                    person = "Ahmed",
                    project = "Negma Project",
                    status = "WAITING",
                    dueDateMillis = now + oneDay
                )
            )
            db.followUpDao().insertFollowUp(
                FollowUpEntity(
                    noteId = 4,
                    subject = "Yasser approval",
                    context = "Waiting for design approval signoff for Medical Center PR 0262",
                    person = "Yasser",
                    project = "Medical Center",
                    status = "DUE",
                    dueDateMillis = now + (2 * oneDay)
                )
            )
            db.followUpDao().insertFollowUp(
                FollowUpEntity(
                    noteId = 1,
                    subject = "Site measurement",
                    context = "Need to confirm final measurements on site with contractor",
                    person = "Ahmed",
                    project = "Negma Project",
                    status = "OPEN",
                    dueDateMillis = now + (3 * oneDay)
                )
            )

            // Seed Reminders
            db.reminderDao().insertReminder(
                ReminderEntity(
                    noteId = 1,
                    title = "Site measurement (Negma project)",
                    scheduledTimeMillis = now + (3600 * 1000 * 4), // 4 hours later today
                    isCompleted = false
                )
            )
            db.reminderDao().insertReminder(
                ReminderEntity(
                    noteId = 5,
                    title = "Doctor appointment (Rheumatology)",
                    scheduledTimeMillis = now + (3600 * 1000 * 7),
                    isCompleted = false
                )
            )
            db.reminderDao().insertReminder(
                ReminderEntity(
                    noteId = 4,
                    title = "Review PR 0262 (Medical Center)",
                    scheduledTimeMillis = now + (3600 * 1000 * 24),
                    isCompleted = false
                )
            )
            } catch (e: Exception) {
                android.util.Log.e("AppDatabase", "Error seeding database", e)
            }
        }
    }
}
