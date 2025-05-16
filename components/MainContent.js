import React, { useMemo } from 'react';
import { View, Text, Image } from 'react-native';

export default function MainContent({ importedData }) {
  const randomGame = useMemo(() => {
    if (!importedData || importedData.length === 0) return null;
    const index = Math.floor(Math.random() * importedData.length);
    return importedData[index];
  }, [importedData]);

  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20 }}>
      {randomGame ? (
        <>
        <Text style={{ fontSize: 18, color: '#ccc', textAlign: 'center' }}>Why not try...</Text>
          <Text style={{ fontSize: 28, fontWeight: 'bold', color: '#fff', textAlign: 'center', marginBottom: 10 }}>
            {randomGame.Name}
          </Text>
          <Text style={{ fontSize: 16, color: '#ccc', textAlign: 'center', marginBottom: 20 }}>
            {randomGame.Genres ? randomGame.Genres.join(', ') : 'No genres available'}
          </Text>
          {randomGame.CoverArtUrl ? (
            <Image
              source={{ uri: randomGame.CoverArtUrl }}
              style={{ width: 200, height: 300, borderRadius: 10 }}
              resizeMode="cover"
            />
          ) : null}
        </>
      ) : (
        <Text style={{ fontSize: 18, color: '#ccc', textAlign: 'center' }}>Import your game file!</Text>
      )}
    </View>
  );
}
