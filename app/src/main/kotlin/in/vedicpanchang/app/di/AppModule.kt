package `in`.vedicpanchang.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.vedicpanchang.app.data.datasource.db.NoteDao
import `in`.vedicpanchang.app.data.datasource.db.NoteDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNoteDatabase(@ApplicationContext context: Context): NoteDatabase =
        NoteDatabase.getInstance(context)

    @Provides
    fun provideNoteDao(db: NoteDatabase): NoteDao = db.noteDao()
}
