import React from 'react';
import { View, Text } from 'react-native';
import { styles } from '../styles';

export default function MainContent() {
  return (
    <View style={styles.content}>
      <Text style={styles.text}>Welcome to GameLibrary</Text>
    </View>
  );
}
