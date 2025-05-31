import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { styles } from '../styles';
import { Ionicons } from '@expo/vector-icons';

const Footer = ({ onImport }) => {
  return (
    <View style={styles.fabContainer}>
      <TouchableOpacity
        style={styles.importFab}
        accessibilityLabel="Import data"
        onPress={onImport}
      >
        <Ionicons name="add-outline" size={24} color="#0b0e1f" />
      </TouchableOpacity>
    </View>
  );
};

export default Footer;
