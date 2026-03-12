import React, { useState } from 'react';
import { View, Text, TouchableOpacity, Modal, TextInput, Alert } from 'react-native';
import { styles } from '../styles';
import { Ionicons } from '@expo/vector-icons';
import * as DocumentPicker from 'expo-document-picker';

const Footer = ({ onImport, onImportUrl }) => {
  const [modalVisible, setModalVisible] = useState(false);
  const [urlInput, setUrlInput] = useState('');

  const handleFilePick = async () => {
    setModalVisible(false);
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: 'application/json',
      });

      if (result.assets && result.assets.length > 0) {
        const file = result.assets[0];
        onImport(file.uri);
      }
    } catch (error) {
      console.error('Failed to pick document:', error);
    }
  };

  const handleUrlImport = () => {
    if (urlInput.trim()) {
      setModalVisible(false);
      onImportUrl(urlInput.trim());
      setUrlInput('');
    } else {
      Alert.alert('Error', 'Please enter a valid URL');
    }
  };

  return (
    <View style={styles.fabContainer}>
      <TouchableOpacity
        style={styles.importFab}
        accessibilityLabel="Import data"
        onPress={() => setModalVisible(true)}
      >
        <Ionicons name="add-outline" size={24} color="#0b0e1f" />
      </TouchableOpacity>

      <Modal
        visible={modalVisible}
        transparent
        animationType="fade"
        onRequestClose={() => setModalVisible(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Import Games</Text>
            
            <TouchableOpacity style={styles.modalButton} onPress={handleFilePick}>
              <Ionicons name="folder-outline" size={20} color="#fff" />
              <Text style={styles.modalButtonText}>Choose File</Text>
            </TouchableOpacity>

            <Text style={styles.modalOrText}>OR</Text>

            <TextInput
              style={styles.urlInput}
              placeholder="Enter JSON URL..."
              placeholderTextColor="#999"
              value={urlInput}
              onChangeText={setUrlInput}
              autoCapitalize="none"
              autoCorrect={false}
              keyboardType="url"
            />
            
            <TouchableOpacity 
              style={[styles.modalButton, !urlInput.trim() && styles.modalButtonDisabled]} 
              onPress={handleUrlImport}
              disabled={!urlInput.trim()}
            >
              <Ionicons name="cloud-download-outline" size={20} color="#fff" />
              <Text style={styles.modalButtonText}>Import from URL</Text>
            </TouchableOpacity>

            <TouchableOpacity 
              style={styles.modalCancelButton} 
              onPress={() => {
                setModalVisible(false);
                setUrlInput('');
              }}
            >
              <Text style={styles.modalCancelText}>Cancel</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
};

export default Footer;
