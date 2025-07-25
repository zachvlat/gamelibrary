import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#272b30',
  },
  screenContainer: {
    flex: 1,
    justifyContent: 'space-between',
    backgroundColor: '#272b30',
  },
  text: {
    fontSize: 20,
    color: '#ffffff',
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#272b30',
  },
  footer: {
    backgroundColor: '#1c1e22',
    padding: 20,
    alignItems: 'center',
  },
  footerText: {
    fontSize: 16,
    marginBottom: 10,
    color: '#ff9510',
  },
  iconContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    width: '100%',
  },
  iconWrapper: {
    alignItems: 'center',
  },
  card: {
    backgroundColor: '#333',
    padding: 10,
    marginBottom: 10,
    borderRadius: 8,
  },
  coverImage: {
    width: '100%',
    height: 150,
    resizeMode: 'cover',
    borderRadius: 8,
    marginBottom: 10,
  },
  rowCard: {
    flexDirection: 'row',
    backgroundColor: '#1c1e22',
    padding: 10,
    marginVertical: 5,
    marginHorizontal: 10,
    borderRadius: 10,
    alignItems: 'center',
  },
  thumbnail: {
    width: 80,
    height: 100,
    borderRadius: 8,
    marginRight: 15,
  },
  cardContent: {
    flex: 1,
    justifyContent: 'center',
  },
  gameTitle: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 5,
  },
searchInput: {
  backgroundColor: '#272b30',
  color: '#fff',
  padding: 15,
  margin: 15,
  borderRadius: 8,
  fontSize: 16,
},
noResultsText: {
  color: '#999',
  textAlign: 'center',
  marginTop: 20,
  fontSize: 16,
},
centeredMessage: {
  flex: 1,
  justifyContent: 'center',
  alignItems: 'center',
},
sourceText: {
  color: '#aaa',
  fontSize: 12,
  marginTop: 4,
},
chipContainer: {
    paddingVertical: 10,
    marginBottom: 5,
    marginTop: 5,
},
chipContentContainer: {
    paddingHorizontal: 10,
    alignItems: 'center',
},
chip: {
    paddingHorizontal: 15,
    paddingVertical: 8,
    borderRadius: 0,
    backgroundColor: '#2d3138',
    marginHorizontal: 5,
},
chipActive: {
    backgroundColor: '#3a7bd5',
},
chipText: {
    color: '#ffffff',
    fontSize: 14,
},
chipTextActive: {
    fontWeight: 'bold',
},
  fabContainer: {
    position: 'absolute',
    bottom: 16,
    right: 16,
    elevation: 6,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 4,
  },
  importFab: {
    backgroundColor: '#55cdff',
    width: 56,
    height: 56,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
  },
  importFabText: {
    color: '#FFFFFF',
    fontSize: 24,
  },
});
