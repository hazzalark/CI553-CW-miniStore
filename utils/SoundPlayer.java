package utils;

import javax.sound.sampled.*; // HL604 Provides classes and interfaces for handling audio, such as playing sound files
import java.io.File; // HL604 Used to handle file paths and access sound files
import java.io.IOException; // HL604 Handles errors during input/output operations

public class SoundPlayer { // HL604 Utility class that handles sound playback
    public static void playSound(String soundFilePath) { // HL604 Plays file sound path.
        try {
            File soundFile = new File(soundFilePath); // HL604 A file object that represents the sound file
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile); // HL604 Opens input stream for reading audio data, supports standard audio formats (.wav)
            Clip clip = AudioSystem.getClip(); // HL604 Creates sound clip
            clip.open(audioStream); // HL604 Loads audio data from AudioInputStream
            clip.start(); // HL604 Starts playing audio
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) { // HL604 Exception Handling
            e.printStackTrace();
        }
    }
}