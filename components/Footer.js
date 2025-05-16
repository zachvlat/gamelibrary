import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { styles } from '../styles';

const footerItems = ['💻 Import'];

export default function Footer({ onImport }) {
  const handlePress = (item) => {
    if (item.includes('Import') && typeof onImport === 'function') {
      onImport();
    }
  };

  return (
    <View style={styles.footer}>
      <View style={styles.iconContainer}>
        {footerItems.map((item, index) => (
          <TouchableOpacity
            key={index}
            style={styles.iconWrapper}
            accessibilityLabel={`Go to ${item}`}
            onPress={() => handlePress(item)}
          >
            <Text style={styles.footerText}>{item}</Text>
          </TouchableOpacity>
        ))}
      </View>
    </View>
  );
}
