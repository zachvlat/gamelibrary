import React from 'react';
import { View, Text, FlatList, Image } from 'react-native';
import { styles } from '../styles';
import gamesData from '../assets/games.json';
import Footer from '../components/Footer';

export default function SourceScreen({ source }) {
    const filteredGames = gamesData.filter(game => game.Sources === source);

    return (
        <View style={styles.screenContainer}>
            <FlatList
                contentContainerStyle={{ paddingBottom: 80 }}
                data={filteredGames}
                keyExtractor={(item) => item.Id}
                renderItem={({ item }) => (
                    <View style={styles.rowCard}>
                        <Image source={{ uri: item.CoverArtUrl }} style={styles.thumbnail} />
                        <View style={styles.cardContent}>
                            <Text style={styles.gameTitle}>{item.Name}</Text>
                            <Text style={styles.text}>
                                {item.Genres ? item.Genres.join(', ') : 'No genres available'}
                            </Text>
                        </View>
                    </View>
                )}
            />
            <Footer />
        </View>
    );
}
