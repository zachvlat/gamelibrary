import React, { useState, useMemo, useEffect } from 'react';
import { NavigationContainer, DefaultTheme } from '@react-navigation/native';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { StatusBar } from 'expo-status-bar';
import { View, Image } from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import * as FileSystem from 'expo-file-system';
import MainContent from './components/MainContent';
import Footer from './components/Footer';
import SourceScreen from './screens/SourceScreen';
import SearchScreen from './screens/SearchScreen';
import AllSourcesScreen from './screens/AllSourcesScreen';
import { styles } from './styles';
import { Ionicons } from '@expo/vector-icons';

const MyTheme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    background: '#1c1e22',
  },
};

const Drawer = createDrawerNavigator();

const DATA_FILE_PATH = FileSystem.documentDirectory + 'importedData.json';

export default function App() {
  const [importedData, setImportedData] = useState([]);

  // Load saved data on app start
  useEffect(() => {
    const loadData = async () => {
      try {
        const fileInfo = await FileSystem.getInfoAsync(DATA_FILE_PATH);
        if (fileInfo.exists) {
          const fileContent = await FileSystem.readAsStringAsync(DATA_FILE_PATH);
          const parsed = JSON.parse(fileContent);
          setImportedData(parsed);
        }
      } catch (error) {
        console.error('Failed to load stored data:', error);
      }
    };

    loadData();
  }, []);

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
          await FileSystem.writeAsStringAsync(DATA_FILE_PATH, JSON.stringify(parsed)); // Write to file
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
        <MainContent importedData={importedData} />
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
        <Drawer.Screen
          name="Home"
          component={HomeScreen}
          options={{
            drawerIcon: () => <Ionicons name="home" size={24} color="#55cdff" />
          }}
        />
        <Drawer.Screen
          name="Search"
          options={{
            drawerIcon: () => <Ionicons name="search" size={24} color="#55cdff" />
          }}
        >
          {() => <SearchScreen importedData={importedData} />}
        </Drawer.Screen>
        <Drawer.Screen
          name="All"
          options={{
            drawerIcon: () => <Ionicons name="library" size={24} color="#55cdff" />
          }}
        >
          {() => <AllSourcesScreen importedData={importedData} />}
        </Drawer.Screen>
        {sources.map((source) => (
          <Drawer.Screen key={source} name={source}>
            {() => <SourceScreen source={source} importedData={importedData} />}
          </Drawer.Screen>
        ))}
      </Drawer.Navigator>
      <StatusBar style="auto" />
    </NavigationContainer>
  );
}