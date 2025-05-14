import React, { useState, useMemo } from 'react';
import { NavigationContainer, DefaultTheme } from '@react-navigation/native';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { StatusBar } from 'expo-status-bar';
import { View, Image } from 'react-native';
import * as DocumentPicker from 'expo-document-picker';

import MainContent from './components/MainContent';
import Footer from './components/Footer';
import SourceScreen from './screen/SourceScreen';
import { styles } from './styles';

const MyTheme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    background: '#1c1e22',
  },
};

const Drawer = createDrawerNavigator();

export default function App() {
  const [importedData, setImportedData] = useState([]);

  const sources = useMemo(() => {
    return [...new Set(importedData.map((game) => game.Sources).filter(Boolean))];
  }, [importedData]);

  const handleImport = async () => {
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: 'application/json',
      });

      if (result.assets && result.assets.length > 0) {
        const file = result.assets[0];
        const fileContent = await fetch(file.uri).then((res) => res.text());
        const parsed = JSON.parse(fileContent);
        if (Array.isArray(parsed)) {
          setImportedData(parsed);
        } else {
          console.warn('Imported file is not a valid array.');
        }
      }
    } catch (error) {
      console.error('Failed to import JSON:', error);
    }
  };

  function HomeScreen() {
    return (
      <View style={styles.screenContainer}>
        <MainContent />
        <Footer onImport={handleImport} />
      </View>
    );
  }

  return (
    <NavigationContainer theme={MyTheme}>
      <Drawer.Navigator
        initialRouteName="Home"
        screenOptions={{
          drawerStyle: { backgroundColor: '#272b30' },
          drawerLabelStyle: { color: '#fff' },
          headerStyle: {
            backgroundColor: '#1c1e22',
            elevation: 0,
            shadowOpacity: 0,
            borderBottomWidth: 0,
          },
          headerTintColor: '#ffffff',
          headerTitleStyle: {
            fontWeight: 'bold',
          },
          headerRight: () => (
            <Image
              source={require('./assets/icon.png')}
              style={{ width: 40, height: 40, marginRight: 15 }}
              resizeMode="contain"
            />
          ),
        }}
      >
        <Drawer.Screen name="Home" component={HomeScreen} />
        {sources.map((source) => (
          <Drawer.Screen key={source} name={source}>
            {() => <SourceScreen source={source} />}
          </Drawer.Screen>
        ))}
      </Drawer.Navigator>
      <StatusBar style="auto" />
    </NavigationContainer>
  );
}
